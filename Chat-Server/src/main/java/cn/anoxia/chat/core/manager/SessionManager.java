package cn.anoxia.chat.core.manager;


import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;


public class SessionManager {
    /**
     * key:会话ID
     * value:设备唯一标识
     */
    private static final ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<>();

    public static void add(String session, String key) {
        sessionMap.put(session, key);
    }

    public static String get(String session) {
        return sessionMap.get(session);
    }

    public static void remove(String session) {
        sessionMap.remove(session);
    }

    public static void removeAll() {
        sessionMap.clear();
    }

    public static boolean exist(String session) {
        if (sessionMap.isEmpty()) {
            return false;
        }
        String equipId = sessionMap.get(session);
        return !(isNull(equipId) || equipId.trim().isEmpty());
    }

    public static int size() {
        return sessionMap.size();
    }

}
