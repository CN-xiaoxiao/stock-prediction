package com.xiaoxiao.stockpredict.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName StockPredictPrice
 * @Description 股票预测 的预测价格数据 实体类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPredictPrice {
    private long id;
    /**日期*/
    private String date;
    /**股票代码*/
    private String symbol;
    /**开盘价*/
    private double open;
    /**收盘价*/
    private double close;
    /**最低价*/
    private double low;
    /**最高价*/
    private double high;
    /**成交量*/
    private double volume;
}
