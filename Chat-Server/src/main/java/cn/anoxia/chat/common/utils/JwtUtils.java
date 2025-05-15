package cn.anoxia.chat.common.utils;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtUtils {




    /**
     * 生成 JWT
     */
    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("login_user_key", UUID.fastUUID().toString());

        String secret = "a_secure_and_long_enough_secret_key_string_here_123456";
        return Jwts.builder()
                .setClaims(claims)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256).compact();
    }


    /**
     * 验证 token 是否有效（未过期且合法）
     */
    public Claims isTokenValid(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 公共方法：解析 JWT Token
     */
    private Claims parseClaims(String token) {
        String secret = "a_secure_and_long_enough_secret_key_string_here_123456";

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}