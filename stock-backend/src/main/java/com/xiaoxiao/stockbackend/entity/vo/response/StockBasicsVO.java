package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockBasicsVO {
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
