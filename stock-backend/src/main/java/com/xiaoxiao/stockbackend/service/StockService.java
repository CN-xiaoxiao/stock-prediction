package com.xiaoxiao.stockbackend.service;

import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

import java.util.Date;
import java.util.List;

public interface StockService {
    StockRealVO getDailyStockData(String tsCode, Date startDate);
    PageInfo<StockBasicsDTO> selectAllBasicsStockDataS(int pageNum, int pageSize);
    List<StockBasicsDTO> selectAllBasicsStockData(int pageNum, int pageSize);
    String saveStockBasics();
    StockBasicsDTO getStockBasicsDTO(String tsCode);
    boolean saveStockMarket(String date, String data);
}
