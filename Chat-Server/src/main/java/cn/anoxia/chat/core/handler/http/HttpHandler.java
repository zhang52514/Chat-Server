package cn.anoxia.chat.core.handler.http;

import cn.anoxia.chat.common.domain.HttpMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class HttpHandler extends OnMessage {
    public HttpHandler(RedisServer redisServer) {
        super(redisServer);
        HttpRouteRegistry.registerRoutes(this);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data) {
        HttpMessage http=(HttpMessage) msg;
        HttpRouteRegistry.dispatch(ctx, http, data);
    }

    @HttpRoute(path = "/user/info")
    public void userInfo(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {
        // 从 msg.getParam() 获取参数
        String uid = (String) msg.getParam().get("uid");
        sendSuccess(ctx,uid);
    }

    @HttpRoute(path = "/system/ping")
    public void ping(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {

    }
}
