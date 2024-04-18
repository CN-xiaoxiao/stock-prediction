package com.xiaoxiao.stockbackend.task;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockMarketJobBean extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//        log.info("正在更新股票交易日...");
//        System.out.println("更新股票交易日中..");
//        log.info("更新股票交易日完成...");
    }
}
