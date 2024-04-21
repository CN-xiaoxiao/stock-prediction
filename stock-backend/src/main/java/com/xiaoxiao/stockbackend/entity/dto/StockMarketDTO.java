package com.xiaoxiao.stockbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockMarketDTO {
    private int id;
    private String date;
    private String data;
}
