package com.xiaoxiao.stockbackend.controller;

import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Resource
    StockService stockService;

    @GetMapping("/daily")
    public RestBean<StockRealVO> getDailyStockData(@RequestParam @Valid String tsCode,
                                                   @RequestParam @Valid
                                                   @DateTimeFormat(pattern = "yyyyMMdd") Date startDate) {
        StockRealVO dailyStockData = stockService.getDailyStockData(tsCode, startDate);
        return RestBean.success(dailyStockData);
    }
}
