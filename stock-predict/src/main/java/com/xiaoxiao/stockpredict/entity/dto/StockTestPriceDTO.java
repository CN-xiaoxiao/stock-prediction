package com.xiaoxiao.stockpredict.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

/**
 * @ClassName StockTestPriceDTO
 * @Description 用于保存到Influxdb中
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@Measurement(name = "test")
public class StockTestPriceDTO {
    /**股票代码*/
    @Column(tag = true)
    private String symbol;
    /**日期*/
    @Column(timestamp = true)
    private Instant date;
    /**开盘价*/
    @Column
    private double open;
    /**收盘价*/
    @Column
    private double close;
    /**最低价*/
    @Column
    private double low;
    /**最高价*/
    @Column
    private double high;
    /**成交量*/
    @Column
    private double volume;
}
