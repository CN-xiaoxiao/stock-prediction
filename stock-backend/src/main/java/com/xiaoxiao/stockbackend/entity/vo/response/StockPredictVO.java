package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

/**
 * @ClassName StockPredictVO
 * @Description 股票预测实体类(数据处理服务器端响应类)
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockPredictVO {
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
