package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.vo.request.ModifyEmailVO;

public interface UserService {
    boolean changePassword(int id, String oldPassword, String newPassword);

    String modifyEmail(int id, ModifyEmailVO vo);

    boolean updateUserAgreement(int id);

    int isUserAgreed(int id);
}
