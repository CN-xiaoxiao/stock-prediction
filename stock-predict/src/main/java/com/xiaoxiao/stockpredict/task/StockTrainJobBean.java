package com.xiaoxiao.stockpredict.task;

import com.xiaoxiao.stockpredict.entity.Const;
import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.dto.StockHistoryPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;
import com.xiaoxiao.stockpredict.model.service.IModelService;
import com.xiaoxiao.stockpredict.service.StockPredictService;
import com.xiaoxiao.stockpredict.utils.InfluxDBUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName StockTrainJobBean
 * @Description 执行定期更新预测模型的任务
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class StockTrainJobBean extends QuartzJobBean {

    @Resource
    StockPredictService stockPredictService;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    IModelService stockPredictWithLSTM;
    @Resource
    InfluxDBUtils influxDBUtils;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<StockData> toBeTrainingStockList = stockPredictService.getToBeTrainingStock();

        log.info("共获取{}条待训练股票.", toBeTrainingStockList.size());
        for (StockData stockData : toBeTrainingStockList) {
            Boolean flag = stringRedisTemplate.hasKey(Const.STOCK_MODEL + stockData.getSymbol());
            if (Boolean.TRUE.equals(flag)) {
                log.info("股票[{}],近一个月内已经训练过模型了,跳过训练...", stockData.getSymbol());
                continue;
            }

            log.info("股票[{}],模型训练开始...", stockData.getSymbol());
            long start = System.currentTimeMillis();

            String stockCode = stockData.getSymbol();
            List<StockHistoryPrice> stockHistoryPriceList = stockPredictService.getTrainingStockData(stockCode);
            log.info("股票[{}],训练数据集大小为:{}", stockCode, stockHistoryPriceList.size());
            if (stockHistoryPriceList.isEmpty()) {
                log.info("未获取到训练数据集，跳过训练！");
                continue;
            }
            List<StockTestPrice> stockTestPrices = stockPredictWithLSTM.modelTrain(stockHistoryPriceList, stockCode);
            try {
                log.info("正在清除[{}]已保存的测试数据", stockCode);
                influxDBUtils.deleteTestData(stockCode);
                log.info("正在保存[{}]新的测试数据,共 {} 条", stockCode, stockTestPrices.size());
                stockTestPrices.forEach(v -> influxDBUtils.writeTestData(v));
            } catch (Exception e) {
                log.warn("股票[{}]的训练数据保存到influxdb失败！,{}", stockCode, e.getMessage());
            }

            long end = System.currentTimeMillis();
            log.info("股票[{}],模型训练完成，共耗时: {} 分钟", stockCode, (end - start) * 1.0 / 1000.0 / 60.0);
            stringRedisTemplate.opsForValue().set(Const.STOCK_MODEL + stockCode, stockCode, 30, TimeUnit.DAYS);
        }

    }
}
