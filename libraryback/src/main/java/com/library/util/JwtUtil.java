package com.library.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private static final String DEFAULT_SECRET = "librarySecretKey2024LibrarySystemSecureKey";
    private static final Long DEFAULT_EXPIRATION = 86400000L;

    @Value("${jwt.secret:librarySecretKey2024LibrarySystemSecureKey}")
    private String secret;
    
    @Value("#{T(java.lang.Long).parseLong('${jwt.expiration:86400000}')}")
    private Long expiration;
    
    private static String staticSecret;
    private static Long staticExpiration;
    
    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            staticSecret = DEFAULT_SECRET;
        } else {
            staticSecret = secret;
        }
        if (expiration == null || expiration <= 0) {
            staticExpiration = DEFAULT_EXPIRATION;
        } else {
            staticExpiration = expiration;
        }
        log.info("JWT工具类初始化完成，密钥长度: {}, 过期时间: {}ms", staticSecret.length(), staticExpiration);
    }
    
    public static String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + staticExpiration);
        
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(staticSecret));
    }
    
    public static DecodedJWT verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(staticSecret)).build();
        return verifier.verify(token);
    }
    
    public static Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }
    
    public static String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("username").asString();
    }
    
    public static Integer getRoleFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("role").asInt();
    }
    
    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public static boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
}
