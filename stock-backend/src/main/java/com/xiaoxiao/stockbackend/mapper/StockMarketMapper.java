package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockMarketMapper {
    @Insert("insert into stock_market values (null, #{date}, #{data})")
    boolean saveStockMarket(String date, String data);

    @Select("select * from stock_market where date = #{date}")
    StockMarketDTO queryStockMarket(String date);
}
