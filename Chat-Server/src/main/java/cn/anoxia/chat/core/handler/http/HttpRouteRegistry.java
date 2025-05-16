package cn.anoxia.chat.core.handler.http;

import cn.anoxia.chat.common.domain.HttpMessage;
import cn.anoxia.chat.common.domain.dto.ResponseDto;
import cn.anoxia.chat.services.IDataHandlerService;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HttpRouteRegistry {
    private static final Map<String, Method> ROUTES = new HashMap<>();
    private static final Map<String, Object> BEANS = new HashMap<>();

    public static void registerRoutes(Object handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            HttpRoute route = method.getAnnotation(HttpRoute.class);
            if (route != null) {
                ROUTES.put(route.path(), method);
                BEANS.put(route.path(), handler);
            }
        }
    }

    public static void dispatch(ChannelHandlerContext ctx, HttpMessage msg, IDataHandlerService data) {
        Method method = ROUTES.get(msg.getPath());
        Object bean = BEANS.get(msg.getPath());
        if (method != null && bean != null) {
            try {
                method.invoke(bean, ctx, msg, data);
            } catch (Exception e) {
                ctx.writeAndFlush(ResponseDto.failure("500", "方法调用失败: " + e.getMessage()));
            }
        } else {
            ctx.writeAndFlush(ResponseDto.failure("404", "路径不存在: " + msg.getPath()));
        }
    }
}
