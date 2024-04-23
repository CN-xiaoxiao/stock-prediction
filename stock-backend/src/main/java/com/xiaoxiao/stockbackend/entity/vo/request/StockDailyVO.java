package com.xiaoxiao.stockbackend.entity.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * @ClassName StockDailyVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class StockDailyVO {
    private String tsCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
