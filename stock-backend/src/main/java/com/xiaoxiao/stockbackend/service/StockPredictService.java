package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

public interface StockPredictService {
    String[] ObjectToStringArr(StockRealVO vo);
    String registerToken();
    boolean verifyAndRegister(String token);
}
