package com.xiaoxiao.stockbackend.controller;

import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AuthorizeService authorizeService;

    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam String email,
                                        @RequestParam String type,
                                        HttpServletRequest request) {
        String message = authorizeService.registerEmailVerifyCode(type, email, request.getRemoteAddr());

        if (message == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(400, message);
        }
    }
}
