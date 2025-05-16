package cn.anoxia.chat.core.handler.chat;


import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.core.manager.ChannelManager;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 消息已读回执处理器
 */
@Component
public class ReadReceiptHandler extends OnMessage {

    private static final Logger log = LoggerFactory.getLogger(ReadReceiptHandler.class);

    public ReadReceiptHandler(RedisServer redisServer) {
        super(redisServer);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data) {
//        //获取更新状态消息ID 自动去重实现
//        List<String> read = new ArrayList<>(new HashSet<>(msg.getRead()));
//        List<String> updateList = new ArrayList<>();
//        if (read.isEmpty()) {
//            ctx.writeAndFlush(ResponseDto.failure("500", "请传递消息ID"));
//            return;
//        }
//        Map<String, MessageStatus> messageIdToNewStatusMap = new HashMap<>();
//        for (String item : read) {
//            messageIdToNewStatusMap.put(item, MessageStatus.READ);
//        }
//        //redis更新消息
//        List<String> messageIds = redisServer.batchUpdateMessageStatus(msg.getRoom().getRoomId(), messageIdToNewStatusMap);
//        boolean areEqualIgnoringOrder = read.size() == messageIds.size() && new HashSet<>(read).containsAll(messageIds);
//
//        if (!areEqualIgnoringOrder) {
//            List<String> difference = read.stream()
//                    .filter(element -> !messageIds.contains(element))
//                    .collect(Collectors.toList());
//            if (!difference.isEmpty()) {
//                //更新DB消息状态
//                int row = data.upMessageStatus(difference, MessageStatus.READ);
//                int count = messageIds.size() + row;
//                if (count == read.size()) {
//                    updateList.addAll(read);
//                }
//            } else {
//                updateList.addAll(messageIds);
//            }
//        } else {
//            updateList.addAll(messageIds);
//        }
//
//        msg.setRead(updateList);
//        sendMessage(ctx, msg, data);
    }

    @Override
    public void sendMessage(ChannelHandlerContext ctx, Object obj, IDataHandlerService data) {
        if (obj instanceof ChatMessage msg) {
            if (StringUtil.isNullOrEmpty(msg.getSenderId()) || StringUtil.isNullOrEmpty(msg.getReceiverId())) {
                ctx.writeAndFlush(ResponseDto.failure("500", "ID不能为空"));
                return;
            }

            //推送到用户
            List<ChannelHandlerContext> ctx_r = ChannelManager.get(msg.getReceiverId());

            if (ctx_r != null && !ctx_r.isEmpty()) {
                sendToClients(ctx_r, msg);  // 推送消息到接收者的客户端
            }

            List<ChannelHandlerContext> ctx_s = ChannelManager.get(msg.getSenderId());
            //给自己回复消息 （支持多设备）
            if (ctx_s != null && !ctx_s.isEmpty()) {
                sendToClients(ctx_s, msg);  // 推送消息到自己的客户端
            }
            return;
        }
        log.error("ReadReceiptHandler Send Type Error:{}", obj.getClass());
    }
}
