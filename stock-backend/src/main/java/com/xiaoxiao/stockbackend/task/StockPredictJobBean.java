package com.xiaoxiao.stockbackend.task;

import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockPredictDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockPredictVO;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @ClassName StockPredictJobBean
 * @Description 定时对用户收藏夹的股票进行预测
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class StockPredictJobBean extends QuartzJobBean {

    @Resource
    private StockPredictService stockPredictService;
    @Resource
    private InfluxDBUtils influxDBUtils;
    @Resource
    private StockService stockService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDate now = LocalDate.now();
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = dtf1.format(now);
        StockMarketDTO stockMarket = stockService.getStockMarket(format);
        String marketData = stockMarket.getData();
        int index = marketData.indexOf(dtf2.format(now));
        String s = "[\"2024-04-04\"";
        if (index > s.length()) { // ["2024-04-04"
            log.info("休市日,不进行预测...");
            return;
        }
        log.info("股票预测任务开始...");
        List<String> tsCodes = stockPredictService.trainingList();
        log.info("共有[{}]条股票待进行预测任务", tsCodes.size());
        int i = 0;
        for (String tsCode : tsCodes) {
            log.info("正在获取第[{}]条股票，股票代码为[{}]的预测信息...", ++i, tsCode);
            List<StockPredictVO> predict = stockPredictService.predict(tsCode);
            StockPredictVO stockPredictVO = predict.get(0);
            StockPredictDTO stockPredictDTO = new StockPredictDTO();

            long sid = stockService.querySidByTsCode(tsCode);
            stockPredictDTO.setSid(sid);

            String date = stockPredictVO.getDate();
            Instant instant = getTrulyPredictDate(date);
            stockPredictDTO.setTradeDate(instant);

            stockPredictDTO.setTsCode(stockPredictVO.getSymbol());
            stockPredictDTO.setOpen(stockPredictVO.getOpen());
            stockPredictDTO.setClose(stockPredictVO.getClose());
            stockPredictDTO.setHigh(stockPredictVO.getHigh());
            stockPredictDTO.setLow(stockPredictVO.getLow());
            stockPredictDTO.setVol(stockPredictVO.getVolume());

            log.info("正在将股票代码为[{}]的数据保存到数据库...", tsCode);
            influxDBUtils.writePredictData(stockPredictDTO);
            log.info("股票代码为[{}]的股票的预测任务完成", tsCode);
        }
    }

    private Instant getTrulyPredictDate(String oldDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(oldDate, dtf);

        while (isStockMarketDay(date)) {
            date = date.plusDays(1);
        }

        return date.atStartOfDay().atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }

    private boolean isStockMarketDay(LocalDate date) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM");
        String format = dtf2.format(date);
        StockMarketDTO stockMarket = stockService.getStockMarket(format);
        String marketData = stockMarket.getData();
        int i = marketData.indexOf(dtf.format(date));
        return i != -1;
    }
}
