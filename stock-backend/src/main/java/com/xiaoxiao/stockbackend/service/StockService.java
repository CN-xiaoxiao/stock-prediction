package com.xiaoxiao.stockbackend.service;

import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockBasicsVO;

import java.util.List;

public interface StockService {
    PageInfo<StockBasicsDTO> selectAllBasicsStockDataS(int pageNum, int pageSize);
    List<StockBasicsDTO> selectAllBasicsStockData(int pageNum, int pageSize);
    String saveStockBasics();
    StockBasicsDTO getStockBasicsDTO(String tsCode);
    int queryStockBasicsCount();
    boolean saveStockMarket(String date, String data);
    StockMarketDTO getStockMarket(String date);
    List<String> getStockTsCode(int pageNum, int pageSize);
    long querySidByTsCode(String tsCode);
    List<StockBasicsVO> getHotStockData();
}
