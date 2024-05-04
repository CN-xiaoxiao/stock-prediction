package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

import java.util.List;

/**
 * @ClassName FavoriteVO
 * @Description 用户股票收藏夹响应实体类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class FavoriteVO {
    int uid;
    List<String> favoriteList;
}
