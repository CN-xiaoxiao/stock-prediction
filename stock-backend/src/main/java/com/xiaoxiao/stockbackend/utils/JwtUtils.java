package com.xiaoxiao.stockbackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 创建JWT令牌
     * @param details
     * @param id
     * @param username
     * @return
     */
    public String createJwt(UserDetails details, int id, String username) {

        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expireTime = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);

        if (token == null) {return null;}

        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            DecodedJWT jwt = verifier.verify(token);
            if (this.isInvalidToken(jwt.getId())) {return null;}

            Date expiresAt = jwt.getExpiresAt();
            return new Date().after(expiresAt) ? null : jwt;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean invalidJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) {return false;}

        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public UserDetails jwtToUserDetails(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();

        return User
                .withUsername(claims.get("name").asString())
                .password("********")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer getId(DecodedJWT jwt) {
        return jwt.getClaim("id").asInt();
    }

    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    private String convertToken(String headerToken) {
        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            return headerToken.substring(7);
        } else {
            return null;
        }
    }

    private boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        stringRedisTemplate
                .opsForValue()
                .set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);

        return true;
    }

    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
