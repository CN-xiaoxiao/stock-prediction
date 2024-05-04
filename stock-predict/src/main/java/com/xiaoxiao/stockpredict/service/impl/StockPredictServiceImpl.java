package com.xiaoxiao.stockpredict.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.Response;
import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.dto.StockHistoryPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockPredictPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;
import com.xiaoxiao.stockpredict.entity.vo.request.StockDailyVO;
import com.xiaoxiao.stockpredict.entity.vo.request.StockHistoryVO;
import com.xiaoxiao.stockpredict.entity.vo.response.StockPredictPriceVO;
import com.xiaoxiao.stockpredict.model.service.IModelService;
import com.xiaoxiao.stockpredict.service.StockPredictService;
import com.xiaoxiao.stockpredict.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName StockPredictServiceImpl
 * @Description 股票预测服务类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Service
public class StockPredictServiceImpl implements StockPredictService {

    @Resource
    IModelService stockPredictWithLSTM;

    @Resource
    NetUtils netUtils;

    /**
     * 从服务器获取待训练的股票列表
     * @return 待训练的股票列表集合
     */
    @Override
    public List<StockData> getToBeTrainingStock() {
        Response response = netUtils.doGet("/trainingList");
        if (!response.success()) {
            return Collections.emptyList();
        }
        List<String> strings = JSONArray.parseArray(response.getData().toString(), String.class);
        List<StockData> stockDataList = new ArrayList<>();
        strings.forEach(v -> {
            StockData sd = new StockData();
            sd.setSymbol(v);
            stockDataList.add(sd);
        });
        return stockDataList;
    }

    @Override
    public List<StockPredictPriceVO> doPredict(StockHistoryVO vo, boolean flag) {
        List<JSONObject> list = vo.getList();

        List<StockHistoryPrice> stockHistoryPriceList = new ArrayList<>();
        for (JSONObject jsonObject : list) {
            StockDailyVO stockDailyVO = JSONObject.parseObject(jsonObject.toJSONString()).to(StockDailyVO.class);
            StockHistoryPrice stockHistoryPrice = getStockHistoryPrice(stockDailyVO);
            stockHistoryPriceList.add(stockHistoryPrice);
        }
        log.info("数据有 {} 条", stockHistoryPriceList.size());
        List<StockPredictPrice> stockPredictPriceList = stockPredictWithLSTM.modelPredict(stockHistoryPriceList, flag);
        List<StockPredictPriceVO> stockPredictPriceVOList = new ArrayList<>();

        this.setStockPredictDate(stockPredictPriceList, stockHistoryPriceList);
        for (StockPredictPrice stockPredictPrice : stockPredictPriceList) {
            StockPredictPriceVO stockPredictPriceVO = new StockPredictPriceVO();
            BeanUtils.copyProperties(stockPredictPrice, stockPredictPriceVO);
            stockPredictPriceVOList.add(stockPredictPriceVO);
        }
        log.info("预测结果有 {} 条", stockPredictPriceVOList.size());
        return stockPredictPriceVOList;
    }

    @Override
    public void doTrain(StockHistoryVO vo) {
        List<JSONObject> list = vo.getList();

        List<StockHistoryPrice> stockHistoryPriceList = new ArrayList<>();
        for (JSONObject jsonObject : list) {
            StockDailyVO stockDailyVO = JSONObject.parseObject(jsonObject.toJSONString()).to(StockDailyVO.class);
            StockHistoryPrice stockHistoryPrice = getStockHistoryPrice(stockDailyVO);
            stockHistoryPriceList.add(stockHistoryPrice);
        }
        String stockCode = stockHistoryPriceList.get(0).getSymbol();
        List<StockTestPrice> stockTestPriceList = stockPredictWithLSTM.modelTrain(stockHistoryPriceList, stockCode);
        stockTestPriceList.forEach(System.out::println);
    }

    @Override
    public List<StockHistoryPrice> getTrainingStockData(String stockCode) {
        Response response = netUtils.doPost("/trainingData", stockCode);
//        System.out.println("response.getData() = " + response.getData());
        List<StockHistoryPrice> stockHistoryPriceList = new ArrayList<>();
        List<StockDailyVO> stockDailyVOS = JSONArray.parseArray(response.getData().toString(), StockDailyVO.class);
        for (StockDailyVO stockDailyVO : stockDailyVOS) {
            StockHistoryPrice stockHistoryPrice = getStockHistoryPrice(stockDailyVO);
            stockHistoryPriceList.add(stockHistoryPrice);
        }
        return stockHistoryPriceList;
    }

    private StockHistoryPrice getStockHistoryPrice(StockDailyVO stockDailyVO) {
        StockHistoryPrice stockHistoryPrice = new StockHistoryPrice();
        stockHistoryPrice.setId(stockDailyVO.getId());
        stockHistoryPrice.setDate(stockDailyVO.getTradeDate());
        stockHistoryPrice.setSymbol(stockDailyVO.getTsCode());
        stockHistoryPrice.setOpen(stockDailyVO.getOpen());
        stockHistoryPrice.setHigh(stockDailyVO.getHigh());
        stockHistoryPrice.setLow(stockDailyVO.getLow());
        stockHistoryPrice.setClose(stockDailyVO.getClose());
        stockHistoryPrice.setVolume(stockDailyVO.getVol());
        return stockHistoryPrice;
    }

    private void setStockPredictDate(List<StockPredictPrice> stockPredictPriceList,
                                     List<StockHistoryPrice> stockHistoryPriceList) {
        int i;
        int exampleLength = 22;
        for (i = 0; i < stockPredictPriceList.size(); i++) {
            if (i + exampleLength < stockHistoryPriceList.size()) {
                stockPredictPriceList.get(i).setDate(stockHistoryPriceList.get(i + exampleLength).getDate());
            } else {
                String date = stockPredictPriceList.get(i-1).getDate();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate date1 = LocalDate.parse(date, dtf);
                date1 = date1.plusDays(1);
                stockPredictPriceList.get(i).setDate(dtf.format(date1));
            }
        }
    }
}
