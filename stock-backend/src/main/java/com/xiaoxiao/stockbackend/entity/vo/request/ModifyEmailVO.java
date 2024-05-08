package com.xiaoxiao.stockbackend.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @ClassName ModifyEmailVO
 * @Description TODO
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
