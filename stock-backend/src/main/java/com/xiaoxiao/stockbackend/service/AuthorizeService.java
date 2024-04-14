package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorizeService extends UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String ip);
}
