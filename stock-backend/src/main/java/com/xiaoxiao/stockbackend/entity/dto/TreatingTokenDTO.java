package com.xiaoxiao.stockbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName TreatingTokenDTO
 * @Description 保存数据处理服务器的token
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class TreatingTokenDTO {
    private Integer id;
    private String token;
}
