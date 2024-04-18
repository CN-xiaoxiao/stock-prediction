package com.xiaoxiao.stockbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Resource
    StockService stockService;
    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    SnowflakeIdGenerator idGenerator;

    @GetMapping("/daily")
    public RestBean<StockRealVO> getDailyStockData(@RequestParam @Valid String tsCode,
                                                   @RequestParam @Valid
                                                   @DateTimeFormat(pattern = "yyyyMMdd") Date startDate) {
        StockRealVO dailyStockData = stockService.getDailyStockData(tsCode, startDate);
        return RestBean.success(dailyStockData);
    }

    @GetMapping("/all-basics2")
    public RestBean<PageInfo<StockBasicsDTO>> getAllStockBasicsDataS(int pageNum, int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockDataS(pageNum, pageSize));
    }

    @GetMapping("/all-basics")
    public RestBean<List<StockBasicsDTO>> getAllStockBasicsData(int pageNum, int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockData(pageNum, pageSize));
    }


    @GetMapping("/save-basics")
    public RestBean<String> saveStockBasicsData() {
        return RestBean.success(stockService.saveStockBasics());
    }

    @GetMapping("/test")
    public RestBean<Void> testInsert() {
        StockBasicsDTO dto = new StockBasicsDTO()
                .setSid(idGenerator.nextId())
                .setTsCode("123").setSymbol("456")
                        .setName("股票").setArea("北京").setIndustry("123").setCnspell("456")
                        .setMarket("上海债务所").setListDate("20241016").setActName("马云").setActEntType("电子");
        stockBasicsMapper.insertStockBasics(dto);
        List<StockBasicsDTO> list = new ArrayList<>();
        list.add(dto);
        list.add(dto);
        String jsonString = JSONObject.toJSONString(list);
        saveJson(jsonString);
        return RestBean.success();
    }
    private void saveJson(String json) {
        try (FileWriter fileWriter = new FileWriter("data.json")) {
            fileWriter.write(json);
        } catch (IOException e) {

        }
    }

    @GetMapping("/test2")
    public RestBean<StockBasicsDTO> testSelect() {
        StockBasicsDTO dto = stockBasicsMapper.selectStockBasicsByTsCode("000001.SZ");
        return RestBean.success(dto);
    }
}
