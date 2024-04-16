package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求真实股票数据的实体类
 */
@Data
@AllArgsConstructor
public class StockRealVO {
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
