package com.xiaoxiao.stockbackend.service.impl;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.mapper.AccountMapper;
import com.xiaoxiao.stockbackend.service.AuthorizeService;
import com.xiaoxiao.stockbackend.utils.Const;
import com.xiaoxiao.stockbackend.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils flowUtils;

    @Resource
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountMapper.findAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public Account findAccountByNameOrEmail(String text) {
        return accountMapper.findAccountByNameOrEmail(text);
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
                return "请求频繁，请稍后再试";
            }

            String code = this.createVerifyCode(6, true);
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail.direct", "mail", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, code, 3, TimeUnit.MINUTES);
            return null;
        }
    }

    private String createVerifyCode(int bit, boolean isNum) {
        Random random = new Random();
        int num = isNum ? 1 : 3;
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < bit; i++) {
            int b = random.nextInt(num);
            switch (b) {
                case 0 ->
                    code.append(random.nextInt(10));
                case 1 ->
                    code.append((char)(random.nextInt(26)+97));
                case 2 ->
                    code.append((char) (random.nextInt(26)+65));
            }
        }
        return code.toString();
    }

    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, verifyLimit);
    }
}
