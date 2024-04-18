package com.xiaoxiao.stockbackend.task;

import com.xiaoxiao.stockbackend.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * 每周自动更新上市股票的任务
 */
@Slf4j
@Component
public class StockBasicsJobBean extends QuartzJobBean {
    @Resource
    StockService stockService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("正在执行更新股票基础数据任务");
        stockService.saveStockBasics();
        log.info("更新股票基础数据完成");
    }
}
