package com.xiaoxiao.stockbackend.controller;

import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockDailyVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockBasicsVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Resource
    StockService stockService;
    @Resource
    StockDailyService stockDailyService;

    // 获得股票基础信息
    @GetMapping("/all-basics2")
    public RestBean<PageInfo<StockBasicsDTO>> getAllStockBasicsDataS(@RequestParam @Valid int pageNum,
                                                                     @RequestParam @Valid int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockDataS(pageNum, pageSize));
    }

    // 获得股票基础信息
    @GetMapping("/all-basics")
    public RestBean<List<StockBasicsDTO>> getAllStockBasicsData(@RequestParam @Valid int pageNum,
                                                                @RequestParam @Valid int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockData(pageNum, pageSize));
    }

    // 获得热门股票列表
    @GetMapping("/hot")
    public RestBean<List<StockBasicsVO>> getHotStockList() {
        return RestBean.success(stockService.getHotStockData());
    }

    // 获取某个股票的日线数据（4个月）
    @GetMapping("/daily")
    public RestBean<List<StockRealVO>> getStockDailyData(@RequestBody @Valid StockDailyVO vo) {
        LocalDate date = vo.getDate();
        LocalDate date1 = date.plusMonths(-4);
        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory(vo.getTsCode(), date1);
        return stockDailyHistory != null && !stockDailyHistory.isEmpty() ?
                RestBean.success(stockDailyHistory):RestBean.failure(400, "暂无数据");
    }

    // 获取某个股票的所有日线数据
    @GetMapping("/daily-all")
    public RestBean<List<StockRealVO>> getAllStockDailyData(@RequestParam @Valid String tsCode) {

        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory(tsCode, LocalDate.parse("2010-01-01"));
        return stockDailyHistory != null && !stockDailyHistory.isEmpty() ?
                RestBean.success(stockDailyHistory):RestBean.failure(400, "暂无数据");
    }

    @GetMapping("/query")
    public RestBean<PageInfo<StockBasicsVO>> queryStockBasics(@RequestParam @Valid int pageNum,
                                                              @RequestParam @Valid int pageSize,
                                                              @RequestParam String tsCode) {
        return RestBean.success(stockService.getStockBasicsVO(pageNum, pageSize, tsCode));
    }

}
