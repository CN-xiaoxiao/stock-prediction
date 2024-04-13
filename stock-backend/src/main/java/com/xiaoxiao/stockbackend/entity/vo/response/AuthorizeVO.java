package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeVO {
    private String username;
    private String token;
    private String role;
    private Date expires;
}
