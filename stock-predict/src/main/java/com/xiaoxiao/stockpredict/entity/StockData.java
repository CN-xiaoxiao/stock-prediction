package com.xiaoxiao.stockpredict.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @ClassName StockData
 * @Description 股票预测模型所使用到的股票价格实体类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StockData {
    /**股票代码*/
    private String symbol;
    /**日期*/
    private String date;
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
