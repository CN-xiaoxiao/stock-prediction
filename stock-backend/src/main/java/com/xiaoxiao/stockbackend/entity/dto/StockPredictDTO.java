package com.xiaoxiao.stockbackend.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

/**
 * @ClassName StockPredictDTO
 * @Description 股票预测数据实体类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@Measurement(name = "predict")
public class StockPredictDTO {
    @Column(tag = true)
    private long sid;
    @Column(timestamp = true)
    private Instant tradeDate;
    @Column
    private String tsCode;
    @Column
    private double open;
    @Column
    private double high;
    @Column
    private double low;
    @Column
    private double close;
    @Column
    private double vol;
}
