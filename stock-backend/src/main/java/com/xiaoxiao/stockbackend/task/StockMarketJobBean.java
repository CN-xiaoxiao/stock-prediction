package com.xiaoxiao.stockbackend.task;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.net.SpiderUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class StockMarketJobBean extends QuartzJobBean {
    @Resource
    SpiderUtils spiderUtils;

    @Resource
    StockService stockService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("正在更新股票交易日...");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        String format = sdf.format(calendar.getTime());
        log.info("获取 {} 的交易日", format);
        try {
            List<String> tradingDay = spiderUtils.getTradingDay(format);
            if (tradingDay != null && !tradingDay.isEmpty()) {
                String jsonString = JSONObject.toJSONString(tradingDay);
                stockService.saveStockMarket(format, jsonString);
            }
        } catch (IOException | InterruptedException e) {
            log.warn("获取股票交易日历失败: {}", e.getMessage());
        }
        log.info("更新股票交易日完成...");
    }
}
