package com.xiaoxiao.stockbackend.mapper;

import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface StockFavoriteMapper {
    @Select("""
    select sf.id, sf.uid, sf.favorite_list from stock_favorite as sf
    	left join account as a
    	on a.id = sf.uid 
	    having sf.uid = #{userId}
    """)
    @Results(id = "favoriteMap", value = {
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "uid", property = "uid", jdbcType = JdbcType.INTEGER),
            @Result(column = "favorite_list", property = "favoriteList", jdbcType = JdbcType.VARCHAR),
    })
    Favorite queryFavoriteByUserID(int userId);

    @Insert("insert into stock_favorite values (null, #{uid}, #{favoriteList})")
    boolean insertFavorite(Favorite favorite);

    @Update("update stock_favorite set favorite_list = #{favoriteList} where uid = #{uid}")
    boolean updateFavorite(Favorite favorite);

    @Select("select * from stock_favorite")
    @ResultMap("favoriteMap")
    List<Favorite> queryAllUserFavorite();
}
