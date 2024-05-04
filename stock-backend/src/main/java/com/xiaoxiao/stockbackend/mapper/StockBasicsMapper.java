package com.xiaoxiao.stockbackend.mapper;


import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface StockBasicsMapper {

    @Select("select * from stock_basics")
    @Results(id = "stockBasicsMap", value = {
        @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
        @Result(column = "ts_code", property = "tsCode", jdbcType = JdbcType.VARCHAR),
        @Result(column = "symbol", property = "symbol", jdbcType = JdbcType.VARCHAR),
        @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
        @Result(column = "area", property = "area", jdbcType = JdbcType.VARCHAR),
        @Result(column = "industry", property = "industry", jdbcType = JdbcType.VARCHAR),
        @Result(column = "cnspell", property = "cnspell", jdbcType = JdbcType.VARCHAR),
        @Result(column = "market", property = "market", jdbcType = JdbcType.VARCHAR),
        @Result(column = "list_date", property = "listDate", jdbcType = JdbcType.VARCHAR),
        @Result(column = "act_name", property = "actName", jdbcType = JdbcType.VARCHAR),
        @Result(column = "act_ent_type", property = "actEntType", jdbcType = JdbcType.VARCHAR)
    })
    List<StockBasicsDTO> queryStockBasicsData();

    @Select("select count(*) from stock_basics")
    int countStockBasics();

    @Insert("insert into stock_basics (id, sid, ts_code, symbol, name, area, industry," +
            " cnspell, market, list_date, act_name, act_ent_type)" +
            "VALUES (null, #{sid}, #{tsCode}, #{symbol}, #{name}, #{area}, #{industry}," +
            " #{cnspell}, #{market}, #{listDate}, #{actName}, #{actEntType})")
    int insertStockBasics(StockBasicsDTO stockBasicsDTO);

    @Select("select * from stock_basics where ts_code = #{tsCode}")
    @ResultMap("stockBasicsMap")
    StockBasicsDTO selectStockBasicsByTsCode(String tsCode);

    @Select("""
        select * from stock_basics
        where
                ts_code like concat('%',#{tsCode},'%')

    """)
    @ResultMap("stockBasicsMap")
    List<StockBasicsDTO> fuzzyQueryStockBasicsDTOByTsCode(String tsCode);

    @Select("select sid from stock_basics where ts_code = #{tsCode}")
    long querySidByTsCode(String tsCode);

    @Select("select ts_code from stock_basics")
    List<String> queryTsCode();
}
