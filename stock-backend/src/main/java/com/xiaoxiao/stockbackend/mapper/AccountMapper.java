package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

@Mapper
public interface AccountMapper {
    @Select("select * from account where username = #{username} or email = #{email}")
    Account findAccountByNameOrEmail(String name);
}
