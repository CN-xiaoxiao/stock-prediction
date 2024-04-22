package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;

import java.time.LocalDate;
import java.util.List;

public interface StockDailyService {
    List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate date);
    List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate startDate, LocalDate endDate);
}
