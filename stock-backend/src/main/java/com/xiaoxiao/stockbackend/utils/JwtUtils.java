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
    //为用户生成Jwt令牌的冷却时间，防止刷接口频繁登录生成令牌，以秒为单位
    @Value("${spring.security.jwt.limit.base}")
    private int limit_base;
    //用户如果继续恶意刷令牌，更严厉的封禁时间
    @Value("${spring.security.jwt.limit.upgrade}")
    private int limit_upgrade;
    //判定用户在冷却时间内，继续恶意刷令牌的次数
    @Value("${spring.security.jwt.limit.frequency}")
    private int limit_frequency;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils utils;

    /**
     * 创建JWT令牌
     * @param details
     * @param id
     * @param username
     * @return
     */
    public String createJwt(UserDetails details, int id, String username) {
        if(this.frequencyCheck(id)) {
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
        } else {
            return null;
        }
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

    /**
     * 频率检测，防止用户高频申请Jwt令牌，并且采用阶段封禁机制
     * 如果已经提示无法登录的情况下用户还在刷，那么就封禁更长时间
     * @param userId 用户ID
     * @return 是否通过频率检测
     */
    private boolean frequencyCheck(int userId){
        String key = Const.JWT_FREQUENCY + userId;
        return utils.limitOnceUpgradeCheck(key, limit_frequency, limit_base, limit_upgrade);
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
