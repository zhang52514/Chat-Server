package cn.anoxia.chat.common.domain.factory;

import cn.anoxia.chat.common.domain.AuthMessage;
import cn.anoxia.chat.common.domain.ChatMessage;
import cn.anoxia.chat.common.domain.HttpMessage;
import cn.anoxia.chat.common.domain.dto.RequestDto;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.common.utils.JwtUtils;
import cn.anoxia.chat.core.manager.TokenManager;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageFactory {
    private static final Map<String, Class<? extends RequestDto>> messageTypeMap = new HashMap<>();

    static {
        messageTypeMap.put("auth", AuthMessage.class);
        messageTypeMap.put("chat", ChatMessage.class);
        messageTypeMap.put("http", HttpMessage.class);
    }

    public static RequestDto parse(String json, String ip, ChannelHandlerContext ctx) {
        JSONObject obj = JSON.parseObject(json);
        String type = obj.getString("cmd");
        Class<? extends RequestDto> clazz = messageTypeMap.get(type);
        if (clazz == null) {
            throw new RuntimeException("Unsupported message type: " + type);
        }
        RequestDto msg = obj.toJavaObject(clazz);
        msg.setTimestamp(new Date());
        msg.setIpAddress(ip);
        //鉴权 token验证
        if (msg instanceof ChatMessage || msg instanceof HttpMessage) {
            boolean tokenValid = JwtUtils.isTokenValid(msg.getToken());
            if (TokenManager.existValues(msg.getToken()) && tokenValid) {
                return msg;
            } else {
                ctx.writeAndFlush(ResponseDto.failure("500", "无权访问"));
            }
        }
        return msg;
    }
}
