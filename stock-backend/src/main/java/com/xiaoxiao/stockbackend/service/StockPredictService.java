package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

import java.util.List;

public interface StockPredictService {
    String[] ObjectToStringArr(StockRealVO vo);
    String registerToken();
    boolean verifyAndRegister(String token);
    List<String> trainingList();
}
