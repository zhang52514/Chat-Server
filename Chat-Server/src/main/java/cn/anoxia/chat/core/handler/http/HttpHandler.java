package cn.anoxia.chat.core.handler.http;

import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.ChatRoom;
import cn.anoxia.chat.common.domain.ChatUser;
import cn.anoxia.chat.common.domain.HttpMessage;
import cn.anoxia.chat.common.domain.dto.ApiResponse;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.core.handler.base.OnMessage;
import cn.anoxia.chat.core.server.RedisServer;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HttpHandler extends OnMessage {
    public HttpHandler(RedisServer redisServer) {
        super(redisServer);
        HttpRouteRegistry.registerRoutes(this);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RequestDto msg, IDataHandlerService data) {
        HttpMessage http = (HttpMessage) msg;
        HttpRouteRegistry.dispatch(ctx, http, data);
    }

    @HttpRoute(path = "/getRooms")
    public void getRooms(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {
        String uid = (String) msg.getParam().get("id");
        if (StringUtil.isNullOrEmpty(uid)) {
            sendFailure(ctx, "500", "用户ID不能为空");
        }
        Map<String, Object> responsePayload = new HashMap<>();
        List<ChatRoom> rooms = data.getRooms(uid);
        responsePayload.put("rooms", rooms);
        sendSuccess(ctx, ApiResponse.with("getRooms", responsePayload));
    }
    @HttpRoute(path = "/getUsers")
    public void getUsers(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {
        String uid = (String) msg.getParam().get("id");
        if (StringUtil.isNullOrEmpty(uid)) {
            sendFailure(ctx, "500", "用户ID不能为空");
        }
        Map<String, Object> responsePayload = new HashMap<>();
        List<ChatUser> users = data.getUsers(uid);
        responsePayload.put("users", users);
        sendSuccess(ctx, ApiResponse.with("getUsers", responsePayload));
    }
    @HttpRoute(path = "/getHistory")
    public void getHistory(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {
        int limit = (int) msg.getParam().get("limit");
        String uid = (String) msg.getParam().get("id");
        if (StringUtil.isNullOrEmpty(uid)) {
            sendFailure(ctx, "500", "用户ID不能为空");
        }
        Map<String, Object> responsePayload = new HashMap<>();
        Map<String, List<ChatMessage>>  messages = data.getAllRoomMessagesWithDbAndAttachments(uid,limit);
        responsePayload.put("messages", messages);
        sendSuccess(ctx, ApiResponse.with("getHistory", responsePayload));
    }

}
