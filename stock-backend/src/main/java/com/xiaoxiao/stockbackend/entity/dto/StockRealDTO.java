package com.xiaoxiao.stockbackend.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Data
@Measurement(name = "real")
public class StockRealDTO {
    @Column(tag = true)
    private long sid;
    @Column
    private String tsCode;
    @Column(timestamp = true)
    private Instant tradeDate;
    @Column
    private double open;
    @Column
    private double high;
    @Column
    private double low;
    @Column
    private double close;
    @Column
    private double preClose;
    @Column
    private double change;
    @Column
    private double pctChg;
    @Column
    private double vol;
    @Column
    private double amount;
}
