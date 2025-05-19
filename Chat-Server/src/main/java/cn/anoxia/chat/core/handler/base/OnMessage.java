package cn.anoxia.chat.core.handler.base;

import cn.anoxia.chat.common.domain.Attachment;
import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.enmu.MessageStatus;
import cn.anoxia.chat.common.enmu.MessageType;
import cn.anoxia.chat.common.utils.ErrorCode;
import cn.anoxia.chat.core.manager.ChannelManager;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public abstract class OnMessage {
    private static final Logger log = LoggerFactory.getLogger(OnMessage.class);
    protected RedisServer redisServer;

    public OnMessage(RedisServer redisServer) {
        this.redisServer = redisServer;
    }

    public abstract void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data);

    public void sendMessage(ChannelHandlerContext ctx, Object obj, IDataHandlerService data) {
        if (obj instanceof ChatMessage msg) {
            //目标ID
            String receiverId = msg.getReceiverId();
            String senderId = msg.getSenderId();

            if (StringUtil.isNullOrEmpty(receiverId) || StringUtil.isNullOrEmpty(senderId)) {
                sendFailure(ctx, ErrorCode.BAD_REQUEST, "参数缺失");
                return;
            }

            String messageId = redisServer.generateDailyId();
            //文件类型设置相关属性
            if (msg.getType() == MessageType.FILE) {
                for (Attachment item : msg.getAttachment()) {
                    item.setId(UUID.randomUUID().toString());
                    item.setMessageId(messageId);
                    item.setCreatedAt(new Date());
                    item.setUpdatedAt(new Date());
                }
            }

            msg.setMessageId(messageId);
            msg.setStatus(MessageStatus.SENT);
            msg.setTimestamp(new Date());

            //判断用户是否离线
            List<ChannelHandlerContext> ctx_r = ChannelManager.get(receiverId);

            if (ctx_r != null && !ctx_r.isEmpty()) {
                msg.setStatus(MessageStatus.DELIVERED);
                sendToClients(ctx_r, msg);  // 推送消息到接收者的客户端
            }

            List<ChannelHandlerContext> ctx_s = ChannelManager.get(senderId);
            //给自己回复消息 （支持多设备）
            if (ctx_s != null && !ctx_s.isEmpty()) {
                sendToClients(ctx_s, msg);  // 推送消息到自己的客户端
            }

            //消息缓存
            cacheMessageToRedis(msg);
            // 处理 msg
        } else {
            // 处理类型不匹配的情况，例如日志输出或返回错误
            log.error("消息类型不匹配：{}", obj.getClass());
            sendFailure(ctx, ErrorCode.BAD_REQUEST, "消息格式错误");
        }
    }

    public String getRemoteIp(ChannelHandlerContext ctx) {
        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
        return socket.getAddress().getHostAddress();
    }

    public void sendFailure(ChannelHandlerContext ctx, String code, String message) {
        ctx.writeAndFlush(ResponseDto.failure(code, message));
    }

    public void sendSuccess(ChannelHandlerContext ctx, Object message) {
        ctx.writeAndFlush(ResponseDto.success(message));
    }

    /**
     * @param ctxList 上下文列表
     * @param msg
     */
    public void sendToClients(List<ChannelHandlerContext> ctxList, ChatMessage msg) {
        for (ChannelHandlerContext item : ctxList) {
            if (item.channel().isActive()) {
                sendSuccess(item, msg);
            }
        }
    }

    public void cacheMessageToRedis(ChatMessage msg) {
        try {
            redisServer.cacheMessage(msg);
        } catch (Exception e) {
            log.error("缓存消息到 Redis 失败", e);
        }
    }

}
