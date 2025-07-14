package cn.anoxia.chat.core.handler.chat;


import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.enmu.MessageStatus;
import cn.anoxia.chat.common.utils.ErrorCode;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.core.manager.ChannelManager;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 聊天连接处理器
 */
@Component
public class CallHandler extends OnMessage {
    private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

    public CallHandler(RedisServer redisServer) {
        super(redisServer);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data) {
        if (!(msg instanceof ChatMessage chatMsg)) {
            sendFailure(ctx, ErrorCode.BAD_REQUEST, "不是 ChatMessage 类型");
            return;
        }

        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();
        if (receiverId == null || senderId == null) {
            sendFailure(ctx, ErrorCode.BAD_REQUEST, "发送方或接收方为空");
            return;
        }

        // 设置默认状态
        chatMsg.setTimestamp(new Date());
        chatMsg.setStatus(MessageStatus.sent);

        // 通话类消息：不入库，仅透传
        List<ChannelHandlerContext> receivers = ChannelManager.get(receiverId);
        if (receivers != null && !receivers.isEmpty()) {
            chatMsg.setStatus(MessageStatus.delivered);
            sendToClients(receivers, chatMsg);
            log.info("[Call] 信令消息推送至 {} 成功", receiverId);
        } else {
            log.warn("[Call] 接收方 {} 不在线，信令未投递", receiverId);
        }

        // 给发送方自己的多设备也返回（比如同步通话弹窗等）
        List<ChannelHandlerContext> senders = ChannelManager.get(senderId);
        if (senders != null && !senders.isEmpty()) {
            sendToClients(senders, chatMsg);
        }
    }
}
