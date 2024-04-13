package com.xiaoxiao.stockbackend.entity.dto;

import lombok.Data;

@Data
public class StockRealDTO {
    private int id;
    private String tsCode;
    private String tradeDate;
    private double open;
    private double high;
    private double low;
    private double close;
    private double preClose;
    private double change;
    private double pctChg;
    private double vol;
    private double amount;
}
