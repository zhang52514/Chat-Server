package cn.anoxia.chat.core.handler.chat;


import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 聊天连接处理器
 */
@Component
public class SingleChatHandler extends OnMessage {
    private static final Logger log = LoggerFactory.getLogger(SingleChatHandler.class);

    public SingleChatHandler(RedisServer redisServer) {
        super(redisServer);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data) {
        super.sendMessage(ctx, msg, data);
    }
}
