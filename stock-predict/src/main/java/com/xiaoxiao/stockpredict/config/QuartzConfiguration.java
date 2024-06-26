package com.xiaoxiao.stockpredict.config;

import com.xiaoxiao.stockpredict.task.StockTrainJobBean;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName QuartzConfiguration
 * @Description 定时任务配置类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Configuration
public class QuartzConfiguration {

    /**
     * 股票训练任务
     * @return
     */
    @Bean("stockTrainJobDetail")
    public JobDetail stockTrainJobDetailBean() {
        return JobBuilder.newJob(StockTrainJobBean.class)
                .withIdentity("stock-train-task")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger stockTrainCronTriggerFactoryBean(@Qualifier("stockTrainJobDetail") JobDetail detail) {
        // 每天凌晨0点30分执行一次 "0 30 0 * * ? *"
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 30 0 * * ? *");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("predict-stock-basics-trigger")
                .withSchedule(cron)
                .build();
    }
}
