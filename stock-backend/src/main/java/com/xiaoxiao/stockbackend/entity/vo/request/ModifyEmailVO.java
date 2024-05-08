package com.xiaoxiao.stockbackend.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @ClassName ModifyEmailVO
 * @Description 封装修改邮件的参数
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class ModifyEmailVO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
}
