package com.xiaoxiao.stockbackend.service.impl;

import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import jakarta.annotation.Resource;

import java.util.Date;
import java.util.List;

/**
 * @ClassName StockDailyServiceImpl
 * @Description 股票交易信息服务类
 * @Author xiaoxiao
 * @Date 2024/4/21 下午8:08
 * @Version 1.0
 */
public class StockDailyServiceImpl implements StockDailyService {

    @Resource
    StockBasicsMapper stockBasicsMapper;

    /**
     * 获取股票编号为 tsCode的股票到 date时间的所有交易记录
     * @param tsCode 股票ts编码
     * @param date 时间日期
     * @return 到date的所有股票交易记录
     */
    @Override
    public List<StockRealDTO> getStockDailyHistory(String tsCode, Date date) {
        long sid = stockBasicsMapper.querySidByTsCode(tsCode);

        return List.of();
    }
}
