package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

@Mapper
public interface AccountMapper {
    @Select("select * from account where username = #{username} or email = #{email}")
    Account findAccountByNameOrEmail(String name);

    @Insert("insert into account (username, password, email, role, image, createtime)" +
            " VALUES (#{username}, #{password}, #{email}, #{role}, #{image}, #{createtime})")
    boolean addAccount(Account account);
}
