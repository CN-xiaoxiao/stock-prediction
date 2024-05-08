package com.xiaoxiao.stockpredict.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName StockPredictPrice
 * @Description 股票预测价格响应类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPredictPriceVO {
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
