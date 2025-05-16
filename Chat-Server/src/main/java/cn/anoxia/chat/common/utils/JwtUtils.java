package cn.anoxia.chat.common.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


public class JwtUtils {

    //密钥
    private static final SecretKey keys = Keys.hmacShaKeyFor("a_secure_and_long_enough_secret_key_string_here_123456".getBytes(StandardCharsets.UTF_8));


    /**
     * token 生成
     */
    public static String generateToken(String info) {
        Claims claims = Jwts.claims();
        claims.setSubject(info);
        claims.setIssuedAt(new Date());

        return Jwts.builder()
                .setClaims(claims)
                .signWith(keys).compact();
    }


    /**
     * 验证 token 是否有效
     */
    public static boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取Token信息
     *
     * @param token token
     * @return 信息
     */
    public static Claims getTokenInfo(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 公共方法：解析 JWT Token
     */
    private static Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keys)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}