package com.xiaoxiao.stockbackend.utils;

import com.alibaba.fastjson2.JSONObject;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 提供一些 InfluxDB 的操作，如增加、查询
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

    @Resource
    StockBasicsMapper stockBasicsMapper;

    @PostConstruct
    public void init() {
        influxDBClient = InfluxDBClientFactory.create(influxUrl, influxUser, influxPassword.toCharArray());
    }

    public StockHistoryVO readRealData(long sid, String startTime, String endTime) {
        StockHistoryVO stockHistoryVO = new StockHistoryVO();

        String query = """
                from(bucket: "%s")
                    |> range(start: %s, stop: %s)
                    |> filter(fn: (r) => r["_measurement"] == "real")
                    |> filter(fn: (r) => r["sid"] == "%s")
                """;
        if (endTime == null) endTime = "now()";
        String format = String.format(query, influxBucket, startTime, endTime, sid);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(format, influxOrg);

        int size = tables.size();
        if (size == 0) return stockHistoryVO;
        List<FluxRecord> records = tables.get(0).getRecords();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        for (int i = 0; i < records.size(); i++) {
            JSONObject object = new JSONObject();

            Instant instant = records.get(i).getTime();
            LocalDate localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault());
            String time = localDate.format(dtf);

            object.put("tradeDate", time);
            for (int j = 0; j < size; j++) {
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(), record.getValue());
            }
            stockHistoryVO.getList().add(object);
        }
        return stockHistoryVO;
    }

    public void writeRealData(StockRealVO stockRealVO) {
        StockRealDTO stockRealDTO = new StockRealDTO();
        BeanUtils.copyProperties(stockRealVO, stockRealDTO);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date= sdf.parse(stockRealVO.getTradeDate());
        } catch (ParseException e) {
            log.error("时间: {} 转换失败", stockRealVO.getTradeDate());
            return;
        }

        stockRealDTO.setTradeDate(date.toInstant());
        String tsCode = stockRealVO.getTsCode();
        if (tsCode == null || tsCode.isEmpty()) return;
        long sid = stockBasicsMapper.querySidByTsCode(tsCode);
        if (sid == 0) {return;}
        stockRealDTO.setSid(sid);

        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
        writeApiBlocking.writeMeasurement(influxBucket, influxOrg, WritePrecision.NS, stockRealDTO);
    }

}
