package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;

import java.util.Date;
import java.util.List;

public interface StockDailyService {
    List<StockRealDTO> getStockDailyHistory(String tsCode, Date date);
}
