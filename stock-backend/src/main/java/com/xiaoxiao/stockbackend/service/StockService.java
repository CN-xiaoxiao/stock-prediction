package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

import java.util.Date;

public interface StockService {
    StockRealVO getDailyStockData(String tsCode, Date startDate);
}
