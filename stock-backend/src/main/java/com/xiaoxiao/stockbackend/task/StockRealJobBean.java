package com.xiaoxiao.stockbackend.task;

import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StockRealJobBean extends QuartzJobBean {

    @Resource
    StockDailyService stockDailyService;
    @Resource
    StockService stockService;
    @Resource
    InfluxDBUtils influxDBUtils;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("正在执行更新每日股票交易信息...");
        Date date = new Date();
        boolean flag = this.isStockMarketDay(date);
        if (flag) {
            log.info("{} 不是交易日, 不进行更新操作", date);
        } else {
            int count = stockService.queryStockBasicsCount();
            int index = count / 1000 + 1;
            for (int i = 0; i < index; i++) {
                List<String> stockTsCode = stockService.getStockTsCode(i + 1, 1000);
                String tsCodes = stockTsCode.stream().map(v-> v + ",").collect(Collectors.joining());
                List<StockRealVO> dailyStockData = stockDailyService.getDailyStockData(tsCodes, date);
                for (StockRealVO stockRealVO : dailyStockData) {
                    influxDBUtils.writeRealData(stockRealVO);
                }
            }
        }
        log.info("更新股票交易信息完成...");
    }

    /**
     * 判断当前日期是否是休市日
     * @param date 当前日期
     * @return true: 是休市日; false: 不是休市日
     */
    private boolean isStockMarketDay(Date date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        StockMarketDTO stockMarket = stockService.getStockMarket(sdf1.format(date));
        String marketData = stockMarket.getData();
        int i = marketData.indexOf(sdf2.format(date));
        return i != -1;
    }
}
