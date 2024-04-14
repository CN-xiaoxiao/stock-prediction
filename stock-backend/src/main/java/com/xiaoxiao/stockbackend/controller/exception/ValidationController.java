package com.xiaoxiao.stockbackend.controller.exception;

import com.xiaoxiao.stockbackend.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validationException(ValidationException e) {
        log.warn("Resolve [{}: {}]", e.getClass().getName(), e.getMessage());
        return RestBean.failure(400, "请求参数有误");
    }
}
