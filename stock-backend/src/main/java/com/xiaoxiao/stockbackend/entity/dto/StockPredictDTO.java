package com.xiaoxiao.stockbackend.entity.dto;

import lombok.Data;

/**
 * @ClassName StockPredictDTO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockPredictDTO {
    private long sid;
    private String date;
    private String tsCode;
    private double open;
    private double close;
    private double low;
    private double high;
    private double vol;
}
