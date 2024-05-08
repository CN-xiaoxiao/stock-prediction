package com.xiaoxiao.stockpredict.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @ClassName StockDailyDTO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDailyDTO {
    private long id;
    /**股票代码*/
    private String symbol;
    /**日期*/
    private Instant date;
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
