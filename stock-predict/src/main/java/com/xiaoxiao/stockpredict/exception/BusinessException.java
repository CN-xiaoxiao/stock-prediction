package com.xiaoxiao.stockpredict.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName BusinessException
 * @Description 自定义异常，处理股票价格方面的异常
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class BusinessException extends RuntimeException{
    private String message;
}
