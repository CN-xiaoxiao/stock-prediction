package com.xiaoxiao.stockpredict.service;

import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.vo.request.StockDailyVO;
import com.xiaoxiao.stockpredict.entity.vo.request.StockHistoryVO;
import com.xiaoxiao.stockpredict.entity.vo.response.StockPredictPriceVO;

import java.util.List;

public interface StockPredictService {
    List<StockData> getStockData();
    List<StockPredictPriceVO> doPredict(StockHistoryVO vo, boolean flag);
    void doTrain(StockHistoryVO vo);
}
