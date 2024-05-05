package com.xiaoxiao.stockpredict.entity.vo.response;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StockTestPredictVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockTestPredictVO {
    List<JSONObject> list = new ArrayList<>();
}
