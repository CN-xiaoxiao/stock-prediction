package com.xiaoxiao.stockbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DataServerController
 * @Description 与数据处理服务器进行交互
 * @Author xiaoxiao
 * @Version 1.0
 */
@RestController
@RequestMapping("/treating")
public class DataTreatingController {
    @Resource
    StockPredictService stockPredictService;
    @Resource
    StockDailyService stockDailyService;

    @GetMapping("/register")
    public RestBean<Void> register(@RequestHeader("Authorization") String token) {
        return stockPredictService.verifyAndRegister(token) ?
                RestBean.success() : RestBean.failure(401, "客户端注册失败，请检查Token是否正确");
    }

    @GetMapping("/trainingList")
    public RestBean<String> trainingList() {
        List<String> strings = stockPredictService.trainingList();
//        List<String> strings = new ArrayList<>();
//        strings.add("000001.SZ");
        return RestBean.success(JSONObject.toJSONString(strings));
    }

    @PostMapping("/trainingData")
    public RestBean<String> test(@RequestBody String json) {
        String tsCode = JSONObject.parseObject(json, String.class);
        List<StockRealVO> stockDailyHistory =
                stockDailyService.getStockDailyHistory(tsCode, LocalDate.parse("2010-01-01"));

        if (stockDailyHistory == null || stockDailyHistory.isEmpty()) {
            return RestBean.failure(400, "股票代码错误！");
        }
        return RestBean.success(JSONObject.toJSONString(stockDailyHistory));
    }
}
