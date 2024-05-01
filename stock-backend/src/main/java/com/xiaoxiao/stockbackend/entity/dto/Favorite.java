package com.xiaoxiao.stockbackend.entity.dto;

import lombok.Data;

/**
 * @ClassName Favorite
 * @Description 用户股票收藏夹实体类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class Favorite {
    private int id;
    private int uid;
    private String favoriteList;
}
