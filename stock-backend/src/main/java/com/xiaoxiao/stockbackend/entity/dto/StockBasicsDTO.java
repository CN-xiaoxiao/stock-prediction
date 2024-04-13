package com.xiaoxiao.stockbackend.entity.dto;

import lombok.Data;

@Data
public class StockBasicsDTO {
    private int id;
    private String tsCode;
    private String symbol;
    private String name;
    private String area;
    private String industry;
    private String cnspell;
    private String market;
    private String listDate;
    private String actName;
    private String actEntType;
}
