package cn.anoxia.chat.services;

import cn.anoxia.chat.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "LOGIN_TOKEN:";

    public String login(String username, String password) {
        // TODO: 替换为数据库验证
        if (!"admin".equals(username) || !"123456".equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(username);

        // 保存到 Redis（设置过期时间）
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, username, 1, TimeUnit.HOURS);

        return token;
    }

    public void logout(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    public boolean isTokenValid(String token) {
        if (jwtUtils.isTokenValid(token) ==null) return false;
        String value = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        return StringUtils.hasText(value);
    }
}
