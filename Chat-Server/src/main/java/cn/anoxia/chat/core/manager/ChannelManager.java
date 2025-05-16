package cn.anoxia.chat.core.manager;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * socket链接
 */
public class ChannelManager {
    /**
     * key：用户标识
     * value：连接通道
     */

    private static final ConcurrentHashMap<String, List<ChannelHandlerContext>> ctxMap = new ConcurrentHashMap<>();

    public static int size() {
        return ctxMap.size();
    }

    public static List<ChannelHandlerContext> get(String key) {
        return ctxMap.get(key);
    }

    public static void add(String key, ChannelHandlerContext ctx) {
        // 获取该用户的连接列表
        List<ChannelHandlerContext> ctxList = ctxMap.computeIfAbsent(key, k -> new ArrayList<>());
        ctxList.add(ctx);
    }


    public static void remove(String key, ChannelHandlerContext ctx) {
        // 获取该用户的所有连接
        List<ChannelHandlerContext> ctxList = get(key);

        // 如果该用户没有任何连接，直接返回
        if (ctxList == null || ctxList.isEmpty()) {
            return;
        }

        // 从该用户的连接列表中移除指定的 ChannelHandlerContext
        ctxList.remove(ctx);

        // 如果移除后该用户的连接列表为空，移除整个用户的记录
        if (ctxList.isEmpty()) {
            ctxMap.remove(key);
        }
    }

    public static void removeAll() {
        ctxMap.clear();
    }


    //检查是否存在
    public static boolean exist(String key) {
        List<ChannelHandlerContext> ctx = ctxMap.get(key);
        return ctx != null && !ctx.isEmpty();
    }
}
