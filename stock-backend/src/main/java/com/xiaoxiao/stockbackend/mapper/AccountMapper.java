package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.entity.vo.request.EmailResetVO;
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

    @Update("update account set password = #{password} where email = #{email}")
    boolean updatePasswordByEmail(EmailResetVO vo);

    @Select("select * from account where id = #{uid}")
    Account findAccountByUid(int uid);

    @Update("update account set password = #{newPassword} where id = #{id}")
    boolean updatePassword(int id, String newPassword);

    @Update("update account set email = #{email} where id = #{id}")
    boolean updateEmailById(int id, String email);
}
