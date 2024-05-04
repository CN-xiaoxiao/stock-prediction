package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.TreatingTokenDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TreatingTokenMapper {
    @Insert("insert into treating_token VALUES (null, #{token})")
    boolean insertToken(TreatingTokenDTO dto);

    @Select("select * from treating_token")
    List<TreatingTokenDTO> selectAllTokens();
}
