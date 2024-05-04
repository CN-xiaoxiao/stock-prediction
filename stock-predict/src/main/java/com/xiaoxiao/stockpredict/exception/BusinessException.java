package com.xiaoxiao.stockpredict.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName BusinessException
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class BusinessException extends RuntimeException{
    private String message;
}
