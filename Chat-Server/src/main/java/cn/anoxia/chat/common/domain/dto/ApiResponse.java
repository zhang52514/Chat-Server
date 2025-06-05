package cn.anoxia.chat.common.domain.dto;

import java.util.HashMap;
import java.util.Map;

public final class ApiResponse {

    private ApiResponse() {
        // 禁止实例化
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<String, Object> base(String path) {
        Map<String, Object> map = new HashMap<>();
        map.put("cmd", "http");
        map.put("path", path);
        return map;
    }

    public static Map<String, Object> of(String path,String key, Object value) {
        Map<String, Object> map = base(path);
        map.put(key, value);
        return map;
    }

    public static Map<String, Object> with(String path,Map<String, Object> extra) {
        Map<String, Object> map = base(path);
        if (extra != null) {
            map.putAll(extra);
        }
        return map;
    }
}
