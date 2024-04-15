package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.entity.vo.request.ConfirmResetVO;
import com.xiaoxiao.stockbackend.entity.vo.request.EmailRegisterVO;
import com.xiaoxiao.stockbackend.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorizeService extends UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String ip);
    String registerEmailAccount(EmailRegisterVO vo);
    String resetConfirm(ConfirmResetVO vo);
    String resetEmailAccountPassword(EmailResetVO vo);
}
