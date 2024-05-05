package com.xiaoxiao.stockpredict.entity.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName StockDailyVO
 * @Description 股票每日信息VO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDailyVO {
    private long id;
    private String tsCode;
    private String tradeDate;
    private double open;
    private double close;
    private double high;
    private double low;
    private double vol;
}
