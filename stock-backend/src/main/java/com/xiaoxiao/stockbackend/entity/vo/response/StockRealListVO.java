package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StockRealListVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockRealListVO {
    List<StockRealVO> list = new ArrayList<>();
}
