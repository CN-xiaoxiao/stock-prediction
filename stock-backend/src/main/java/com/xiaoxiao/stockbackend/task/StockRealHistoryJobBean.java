package com.xiaoxiao.stockbackend.task;

import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName StockRealHistoryJobBean
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class StockRealHistoryJobBean extends QuartzJobBean {

    @Resource
    StockDailyService stockDailyService;
    @Resource
    StockService stockService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("正在执行更新历史交易数据操作");
        long startTime = System.currentTimeMillis();

        List<String> stockTsCodeList = stockService.getStockTsCode();
        for (String s : stockTsCodeList) {
            log.info("更新股票代码为: {} 的历史交易信息", s);
            stockDailyService.updateStockDailyHistory(s);
        }

        long endTime = System.currentTimeMillis();
        log.info("执行更新历史交易数据操作完成，共耗时 {} s", (endTime - startTime)*1.0 / 1000.0);
    }
}
