package com.xiaoxiao.stockbackend.service;

import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.FavoriteVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockBasicsVO;

import java.util.List;

public interface StockService {
    PageInfo<StockBasicsDTO> selectAllBasicsStockDataS(int pageNum, int pageSize);
    List<StockBasicsDTO> selectAllBasicsStockData(int pageNum, int pageSize);
    String saveStockBasics();
    List<StockBasicsDTO> getStockBasicsDTO(String tsCode);
    PageInfo<StockBasicsVO> getStockBasicsVO(int pageNum, int pageSize, String tsCode);
    int queryStockBasicsCount();
    boolean saveStockMarket(String date, String data);
    boolean saveStockMarket(String date);
    StockMarketDTO getStockMarket(String date);
    List<String> getStockTsCode(int pageNum, int pageSize);
    List<String> getStockTsCode();
    long querySidByTsCode(String tsCode);
    List<StockBasicsVO> getHotStockData();
    FavoriteVO queryFavoriteByUid(int uid);
    boolean updateFavorite(Favorite favorite);
    boolean insertFavorite(Favorite favorite);
    boolean addFavorite(String tsCode, String token);
}
