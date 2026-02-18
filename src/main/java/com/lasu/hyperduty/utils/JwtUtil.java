package com.lasu.hyperduty.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpirationTime = 3600000; // 1小时
    private final long refreshTokenExpirationTime = 604800000; // 7天

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 生成RSA密钥对
    public JwtUtil() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // 使用2048位RSA密钥
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * 生成访问令牌
     * @param username 用户名
     * @return 访问令牌
     */
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, accessTokenExpirationTime);
    }

    /**
     * 生成包含用户信息的访问令牌
     * @param username 用户名
     * @param employeeId 员工ID
     * @param name 员工姓名
     * @return 访问令牌
     */
    public String generateAccessToken(String username, Long employeeId, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeId", employeeId);
        claims.put("name", name);
        return createToken(claims, username, accessTokenExpirationTime);
    }

    /**
     * 生成刷新令牌
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        String refreshToken = createToken(claims, username, refreshTokenExpirationTime);
        
        // 将刷新令牌存储到Redis中，用于验证和管理
        redisTemplate.opsForValue().set(
                "refresh_token:" + username,
                refreshToken,
                refreshTokenExpirationTime,
                TimeUnit.MILLISECONDS
        );
        
        return refreshToken;
    }

    /**
     * 创建令牌
     * @param claims 声明
     * @param subject 主题
     * @param expirationTime 过期时间
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 使用RSA256算法签名
                .compact();
    }

    /**
     * 从令牌中提取用户名
     * @param token 令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 从令牌中提取过期时间
     * @param token 令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * 从令牌中提取所有声明
     * @param token 令牌
     * @return 声明
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(publicKey) // 使用公钥验证签名
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查令牌是否过期
     * @param token 令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 验证访问令牌
     * @param token 令牌
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateAccessToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token) && !isTokenBlacklisted(token));
    }

    /**
     * 验证刷新令牌
     * @param token 刷新令牌
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateRefreshToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        if (!extractedUsername.equals(username) || isTokenExpired(token)) {
            return false;
        }
        
        // 验证刷新令牌是否与Redis中存储的一致
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("refresh_token:" + username);
        return token.equals(storedRefreshToken);
    }

    /**
     * 将令牌加入黑名单
     * @param token 令牌
     */
    public void blacklistToken(String token) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        Date expiration = claims.getExpiration();
        long timeUntilExpiration = expiration.getTime() - new Date().getTime();
        
        if (timeUntilExpiration > 0) {
            redisTemplate.opsForValue().set(
                    "blacklisted_token:" + token,
                    username,
                    timeUntilExpiration,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 检查令牌是否在黑名单中
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public Boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklisted_token:" + token);
    }

    /**
     * 登出用户，将令牌加入黑名单并删除刷新令牌
     * @param accessToken 访问令牌
     * @param username 用户名
     */
    public void logout(String accessToken, String username) {
        // 将访问令牌加入黑名单
        blacklistToken(accessToken);
        
        // 删除刷新令牌
        redisTemplate.delete("refresh_token:" + username);
    }

    /**
     * 使用刷新令牌生成新的访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        Claims claims = extractAllClaims(refreshToken);
        String username = claims.getSubject();
        
        // 验证刷新令牌
        if (!validateRefreshToken(refreshToken, username)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // 生成新的访问令牌
        return generateAccessToken(username);
    }

    /**
     * 使用包含用户信息的刷新令牌生成新的访问令牌
     * @param refreshToken 刷新令牌
     * @param employeeId 员工ID
     * @param name 员工姓名
     * @return 新的访问令牌
     */
    public String refreshAccessToken(String refreshToken, Long employeeId, String name) {
        Claims claims = extractAllClaims(refreshToken);
        String username = claims.getSubject();
        
        // 验证刷新令牌
        if (!validateRefreshToken(refreshToken, username)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // 生成新的访问令牌
        return generateAccessToken(username, employeeId, name);
    }

}