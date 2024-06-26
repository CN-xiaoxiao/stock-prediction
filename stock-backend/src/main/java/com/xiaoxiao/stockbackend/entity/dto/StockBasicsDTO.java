package com.xiaoxiao.stockbackend.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockBasicsDTO {
    private int id;
    private long sid;
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
