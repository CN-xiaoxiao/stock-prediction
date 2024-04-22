package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface StockDailyService {
    List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate date);
    List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate startDate, LocalDate endDate);
    List<StockRealVO> getDailyStockData(String tsCode, Date startDate);
}
