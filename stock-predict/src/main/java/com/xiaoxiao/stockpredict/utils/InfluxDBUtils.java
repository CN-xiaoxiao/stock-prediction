package com.xiaoxiao.stockpredict.utils;

import com.alibaba.fastjson2.JSONObject;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPriceDTO;
import com.xiaoxiao.stockpredict.entity.vo.response.StockTestPredictVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @ClassName InfluxdbUtils
 * @Description influxdb的工具类，保护读写删除操作
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class InfluxDBUtils {
    @Value("${spring.influx.url}")
    String influxUrl;
    @Value("${spring.influx.user}")
    String influxUser;
    @Value("${spring.influx.password}")
    String influxPassword;
    @Value("${spring.influx.bucket}")
    String influxBucket;
    @Value("${spring.influx.org}")
    String influxOrg;

    private InfluxDBClient influxDBClient;

    @PostConstruct
    public void init() {
        influxDBClient = InfluxDBClientFactory.create(influxUrl, influxUser, influxPassword.toCharArray());
    }

    public void writeTestData(StockTestPrice stockTestPrice) {
        if (stockTestPrice == null) return;

        StockTestPriceDTO stockTestPriceDTO = new StockTestPriceDTO();
        BeanUtils.copyProperties(stockTestPrice, stockTestPriceDTO);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date= sdf.parse(stockTestPrice.getDate());
        } catch (ParseException e) {
            log.error("时间: {} 转换失败", stockTestPrice.getDate());
            return;
        }
        stockTestPriceDTO.setDate(date.toInstant());

        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
        writeApiBlocking.writeMeasurement(influxBucket, influxOrg, WritePrecision.NS, stockTestPriceDTO);
    }

    /**
     * 读取股票测试数据的信息
     * @param stockCode 股票代码
     * @param startTime 开始时间 相对时间(-1d: 过去一天)或绝对时间（格式: yyyy-MM-dd）
     * @param endTime 结束时间
     * @return StockTestPredictVO 包装类，里面存的是 StockTestPriceDTO 的 JSONObject
     */
    public StockTestPredictVO readTestData(String stockCode, String startTime, String endTime) {
        StockTestPredictVO stockTestPredictVO = new StockTestPredictVO();
        String query;

        if (endTime == null || endTime.isEmpty()) {
            endTime = "now()";
            query = "from(bucket: \"%s\") " +
                    "|> range(start: %s, stop: %s)" +
                    "|> filter(fn: (r) => r[\"_measurement\"] == \"test\")" +
                    "|> filter(fn: (r) => r[\"symbol\"] == \"%s\")";
        } else {
            query = "from(bucket: \"%s\") " +
                    "|> range(start: time(v: \"%s\"), stop: time(v: \"%s\"))" +
                    "|> filter(fn: (r) => r[\"_measurement\"] == \"test\")" +
                    "|> filter(fn: (r) => r[\"symbol\"] == \"%s\")";
        }
        String format = String.format(query, influxBucket, startTime, endTime, stockCode);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(format, influxOrg);

        int size = tables.size();
        if (size == 0) return stockTestPredictVO;
        List<FluxRecord> records = tables.get(0).getRecords();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (int i = 0; i < records.size(); i++) {
            JSONObject object = new JSONObject();

            Instant instant = records.get(i).getTime();
            LocalDate localDate = instant.atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
            String time = localDate.format(dtf);

            object.put("date", time);
            for (int j = 0; j < size; j++) {
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(), record.getValue());
            }
            stockTestPredictVO.getList().add(object);
        }
        return stockTestPredictVO;
    }

    public void deleteTestData(String stockCode) {
        DeletePredicateRequest deletePredicateRequest = new DeletePredicateRequest();
        deletePredicateRequest.setPredicate("_measurement=\"test\" AND symbol=\"" + stockCode + "\"");
        deletePredicateRequest.start(OffsetDateTime.parse("2010-01-10T00:00:00+08:00"));
        deletePredicateRequest.stop(OffsetDateTime.now());
        influxDBClient.getDeleteApi().delete(deletePredicateRequest, influxBucket, influxOrg);
    }
}
