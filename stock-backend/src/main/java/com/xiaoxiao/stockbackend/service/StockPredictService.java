package com.xiaoxiao.stockbackend.service;

import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockPreVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockPredictVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;

import java.time.LocalDate;
import java.util.List;

public interface StockPredictService {
    String[] ObjectToStringArr(StockRealVO vo);
    String registerToken();
    boolean verifyAndRegister(String token);
    /**
     * 获取所有用户收藏夹中的股票代码
     * @return 股票代码集合
     */
    List<String> trainingList();
    List<StockPredictVO> getPredictData(List<StockRealVO> stockRealVOList, boolean flag);
    List<StockPredictVO> predict(String tsCode);

    /**
     * 从数据库中获取预测的股票数据
     * @param tsCode ts股票代码
     * @param start 开始时间
     * @param end 结束时间
     * @return 预测数据集合
     */
    List<StockPreVO> getPredictList(String tsCode, LocalDate start, LocalDate end);
}
