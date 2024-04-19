package com.xiaoxiao.stockbackend.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotStockVO {
    private String tsCode;
    private String name;
}
