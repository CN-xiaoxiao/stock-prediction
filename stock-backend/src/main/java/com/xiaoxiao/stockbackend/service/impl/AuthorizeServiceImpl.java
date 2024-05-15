package com.xiaoxiao.stockbackend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.entity.vo.request.ConfirmResetVO;
import com.xiaoxiao.stockbackend.entity.vo.request.EmailRegisterVO;
import com.xiaoxiao.stockbackend.entity.vo.request.EmailResetVO;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
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

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        String username = vo.getUsername();
        if (this.existsAccountByEmail(email)) return "该邮箱已被注册，请使用其他邮件地址";
        if (this.existsAccountByName(username)) return "该用户名已被使用，请使用其他用户名";
        String password = passwordEncoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email,
                "user", null, new Date(), 0);
        boolean flag = accountMapper.addAccount(account);
        if (flag) {
            this.deleteEmailVerifyCode(email);
            return null;
        } else {
            return "服务器内部错误，注册失败";
        }
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if (verify != null) return verify;
        vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        boolean flag = accountMapper.updatePasswordByEmail(vo);
        if (flag) {
            deleteEmailVerifyCode(email);
            this.askSuccessEmail(email);
            return null;
        }
        return "更新失败，请联系管理员";
    }

    private void askSuccessEmail(String email) {
        Account account = accountMapper.findAccountByNameOrEmail(email);
        String jsonString = JSONObject.toJSONString(account);
        Map<String, Object> data = Map.of("type", "resetSuccess", "email", email, "account", jsonString);
        amqpTemplate.convertAndSend("mail.direct", "mail", data);
    }


    private void deleteEmailVerifyCode(String email) {
        stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
    }

    private boolean existsAccountByName(String username) {
        Account account = accountMapper.findAccountByNameOrEmail(username);
        return account != null;
    }

    private boolean existsAccountByEmail(String email) {
        Account account = accountMapper.findAccountByNameOrEmail(email);
        return account != null;
    }

    private String getEmailVerifyCode(String email) {
        return stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
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
