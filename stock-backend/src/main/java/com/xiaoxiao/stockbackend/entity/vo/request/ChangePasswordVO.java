package com.xiaoxiao.stockbackend.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @ClassName ChangePasswordVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class ChangePasswordVO {
    @Length(min = 6, max = 20)
    String password;
    @Length(min = 6, max = 20)
    String new_password;
}
