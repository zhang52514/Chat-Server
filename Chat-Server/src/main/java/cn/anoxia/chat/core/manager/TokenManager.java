package cn.anoxia.chat.core.manager;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

/**
 * 用户Token管理
 * */
public class TokenManager {
    private static final ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<>();
    public static void add(String id, String token) {
        tokenMap.put(id, token);
    }

    public static String get(String id) {
        return tokenMap.get(id);
    }

    public static void remove(String id) {
        tokenMap.remove(id);
    }

    public static void removeAll() {
        tokenMap.clear();
    }

    public static boolean exist(String id) {
        if (tokenMap.isEmpty()) {
            return false;
        }
        String token = tokenMap.get(id);
        return !(isNull(token) || token.trim().isEmpty());
    }

    public static boolean existValues(String token) {
        if (tokenMap.isEmpty()) {
            return false;
        }
        return tokenMap.containsValue(token);
    }

    public static int size() {
        return tokenMap.size();
    }
}
