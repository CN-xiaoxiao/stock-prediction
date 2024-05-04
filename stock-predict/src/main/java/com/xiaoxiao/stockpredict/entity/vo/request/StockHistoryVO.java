package com.xiaoxiao.stockpredict.entity.vo.request;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StockHistoryVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockHistoryVO {
    List<JSONObject> list = new ArrayList<>();
}
