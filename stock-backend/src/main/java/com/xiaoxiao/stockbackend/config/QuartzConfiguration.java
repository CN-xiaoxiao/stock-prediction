package com.xiaoxiao.stockbackend.config;

import com.xiaoxiao.stockbackend.task.*;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {
    /**
     * 获取股票基础信息
     * @return
     */
    @Bean("stockBasicsJobDetail")
    public JobDetail stockBasicsJobDetailBean() {
        return JobBuilder.newJob(StockBasicsJobBean.class)
                .withIdentity("predict-stock-basics-task")
                .storeDurably()
                .build();
    }

    /**
     * 获取交易日
     * @return
     */
    @Bean("stockMarketJobDetail")
    public JobDetail stockMarketJobDetailBean() {
        return JobBuilder.newJob(StockMarketJobBean.class)
                .withIdentity("predict-stock-market-task")
                .storeDurably()
                .build();
    }

    /**
     * 获取股票真实交易信息
     * @return
     */
    @Bean("stockRealJobDetail")
    public JobDetail stockRealJobDetailBean() {
        return JobBuilder.newJob(StockRealJobBean.class)
                .withIdentity("predict-stock-real-task")
                .storeDurably()
                .build();
    }

    /**
     * 定时更新股票交易的历史数据
     * @return
     */
    @Bean("stockRealHistoryJobDetail")
    public JobDetail stockRealHistoryJobDetailBean() {
        return JobBuilder.newJob(StockRealHistoryJobBean.class)
                .withIdentity("predict-stock-real-history-task")
                .storeDurably()
                .build();
    }

    /**
     * 获取股票交易热门榜单
     * @return
     */
    @Bean("hotStockJobDetail")
    public JobDetail hotStockJobDetailBean() {
        return JobBuilder.newJob(HotStockJobBean.class)
                .withIdentity("predict-stock-hot-task")
                .storeDurably()
                .build();
    }

    @Bean("predictStockJobDetail")
    public JobDetail predictStockJobDetailBean() {
        return JobBuilder.newJob(StockPredictJobBean.class)
                .withIdentity("predict-stock-task")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger stockBasicsCronTriggerFactoryBean(@Qualifier("stockBasicsJobDetail") JobDetail detail) {
        // 每天22点执行一次 ‘0 0 22 1/1 * ? *’
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 0 22 1/1 * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-basics-trigger")
                .withSchedule(cron)
                .build();
    }

    @Bean
    public Trigger stockMarketCronTriggerFactoryBean(@Qualifier("stockMarketJobDetail") JobDetail detail) {
        // 每月1号，15号凌晨0点5分，执行一次 ‘0 5 0 1,15 * ? *’
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 5 0 1,15 * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-market-trigger")
                .withSchedule(cron)
                .build();
    }

    @Bean
    public Trigger stockRealCronTriggerFactoryBean(@Qualifier("stockRealJobDetail") JobDetail detail) {
        // 每天下午16点10分自动执行 ‘0 10 16 * * ? *’
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 10 16 * * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-real-trigger")
                .withSchedule(cron)
                .build();
    }

    @Bean
    public Trigger stockRealHistoryCronTriggerFactoryBean(@Qualifier("stockRealHistoryJobDetail") JobDetail detail) {
        // 每天凌晨0点30分自动执行 ‘0 30 0 * * ? *’
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 30 0 * * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-real-history-trigger")
                .withSchedule(cron)
                .build();
    }

    @Bean
    public Trigger hotStockCronTriggerFactoryBean(@Qualifier("hotStockJobDetail") JobDetail detail) {
        // 早上9:30到11:30，下午13:00到15:00，以及中午11:30到13:00 每30分钟执行一次
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 0/30 9,10,11,12,13,14,15 * * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-hot-trigger")
                .withSchedule(cron)
                .build();
    }

    @Bean
    public Trigger predictStockCronTrigger(@Qualifier("predictStockJobDetail") JobDetail detail) {
        // 每天晚上11点30分开始执行 '0 30 11 * * ? *'
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 30 11 * * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-trigger")
                .withSchedule(cron)
                .build();
    }
}
