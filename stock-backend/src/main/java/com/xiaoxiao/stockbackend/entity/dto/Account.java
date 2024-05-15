package com.xiaoxiao.stockbackend.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoxiao.stockbackend.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Account implements BaseData {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String role;
    private String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;
    private Integer agreed;
}
