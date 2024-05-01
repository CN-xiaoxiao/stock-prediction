package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

import java.util.List;

/**
 * @ClassName FavoriteVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class FavoriteVO {
    int uid;
    List<String> favoriteList;
}
