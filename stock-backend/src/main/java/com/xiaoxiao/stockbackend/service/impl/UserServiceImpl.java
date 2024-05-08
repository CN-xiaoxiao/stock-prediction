package com.xiaoxiao.stockbackend.service.impl;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.entity.vo.request.ModifyEmailVO;
import com.xiaoxiao.stockbackend.mapper.AccountMapper;
import com.xiaoxiao.stockbackend.service.UserService;
import com.xiaoxiao.stockbackend.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    AccountMapper accountMapper;
    @Resource
    PasswordEncoder passwordEncoder;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean changePassword(int id, String oldPassword, String newPassword) {
        Account account = accountMapper.findAccountByUid(id);
        if (account == null) return false;
        String password = account.getPassword();
        if (!passwordEncoder.matches(oldPassword, password)) return false;

        return accountMapper.updatePassword(id, passwordEncoder.encode(newPassword));
    }

    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String code = this.getEmailVerifyCode(vo.getEmail());
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        this.deleteEmailVerifyCode(vo.getEmail());
        Account account = accountMapper.findAccountByNameOrEmail(vo.getEmail());
        if (account != null && account.getId() != id) return "该邮箱账户已经被其他账户所绑定，无法完成操作";
        boolean flag = accountMapper.updateEmailById(id, vo.getEmail());
        if (!flag) return "服务器内部错误";
        return null;
    }

    /**
     * 获取Redis中存储的邮件验证码
     * @param email 电子邮件
     * @return 验证码
     */
    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 移除Redis中存储的邮件验证码
     * @param email 电子邮件
     */
    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }
}
