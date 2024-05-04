package com.xiaoxiao.stockpredict.model.service;

import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.dto.StockHistoryPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockPredictPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;

import java.util.List;

public interface IModelService {
    /**
     * 模型训练，增量训练或者初始化训练
     */
    List<StockTestPrice> modelTrain(List<StockHistoryPrice> stockHistoryPriceList, String stockCode);

    /**
     * 模型预测
     */
    List<StockPredictPrice> modelPredict(List<StockHistoryPrice> stockHistoryPriceList, boolean flag);
}
