package cn.anoxia.chat.common.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void setValue(String key, Object value) {
        Objects.requireNonNull(key, "Redis key must not be null");
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getValue(String key) {
        Objects.requireNonNull(key, "Redis key must not be null");
        return redisTemplate.opsForValue().get(key);
    }

    public void setValueWithExp(String key, Object value, long timeOut, TimeUnit timeUnit) {
        Objects.requireNonNull(key, "Redis key must not be null");
        redisTemplate.opsForValue().set(key, value, timeOut, timeUnit);
    }

    public void deleteValue(String key) {
        Objects.requireNonNull(key, "Redis key must not be null");
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        Objects.requireNonNull(key, "Redis key must not be null");
        Boolean hasKey = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    public void remove(String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
}
