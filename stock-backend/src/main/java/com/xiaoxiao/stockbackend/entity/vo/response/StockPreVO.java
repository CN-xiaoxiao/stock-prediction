package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.Data;

/**
 * @ClassName StockPreVO
 * @Description 股票预测数据的响应类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockPreVO {
    private String tradeDate;
    private String tsCode;
    private double open;
    private double close;
    private double low;
    private double high;
    private double vol;
}
