package com.xiaoxiao.stockbackend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.mapper.StockMarketMapper;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import com.xiaoxiao.stockbackend.utils.net.NetUtils;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName StockDailyServiceImpl
 * @Description 股票交易信息服务类
 * @Author xiaoxiao
 * @Date 2024/4/21 下午8:08
 * @Version 1.0
 */
@Service
public class StockDailyServiceImpl implements StockDailyService {

    private static final Logger log = LoggerFactory.getLogger(StockDailyServiceImpl.class);
    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    StockMarketMapper stockMarketMapper;
    @Resource
    InfluxDBUtils influxDBUtils;
    @Resource
    NetUtils netUtils;

    /**
     * 获取股票编号为 tsCode的股票从 date 到 now() 时间的所有交易记录
     * @param tsCode 股票ts编码
     * @param date 时间日期
     * @return date到now()的所有股票交易记录
     */
    @Override
    public List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate date) {
        List<StockRealDTO> result = new ArrayList<>();

        long sid = stockBasicsMapper.querySidByTsCode(tsCode);
        if (sid <= 0) return null;

        long diffDay = getDifferDayTime(date, LocalDate.now());
        String time = diffDay -1 + "d";

        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(sid, time, null);
        return getStockRealDTOS(result, sid, stockHistoryVO);
    }

    /**
     * 获取股票代码为tsCode的股票，从 startDate 到 endDate内的所有股票交易数据
     * @param tsCode 股票ts代码
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 从 startDate 到 endDate 之间的股票交易数据
     */
    @Override
    public List<StockRealDTO> getStockDailyHistory(String tsCode, LocalDate startDate, LocalDate endDate) {
        List<StockRealDTO> result = new ArrayList<>();

        long sid = stockBasicsMapper.querySidByTsCode(tsCode);
        if (sid <= 0) return null;

        startDate = startDate.plusDays(-1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(sid, startDate.format(dtf), endDate.format(dtf));
        return getStockRealDTOS(result, sid, stockHistoryVO);
    }

    /**
     * 从第三方api得到每天的股票交易数据
     * @param tsCode 股票ts代码
     * @param startDate 开始时间
     * @return
     */
    @Override
    public List<StockRealVO> getDailyStockData(String tsCode, Date startDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(startDate);

        Map<String, String> params = Map.of("ts_code", tsCode, "start_date", date, "end_date", date);
        StockApiVO apiVO = netUtils.createApiVO("daily", netUtils.getToken(), params, null);

        StockApiResponse response;
        try {
            response = netUtils.doPost(apiVO);
        } catch (IOException | InterruptedException e) {
            log.warn("获取每日股票交易信息出错");
            return null;
        }

        return Objects.requireNonNull(response.getItems(StockRealVO.class));
    }

    @Nullable
    private List<StockRealDTO> getStockRealDTOS(List<StockRealDTO> result, long sid, StockHistoryVO stockHistoryVO) {
        List<JSONObject> list = stockHistoryVO.getList();
        if (list == null || list.isEmpty()) return null;
        for (JSONObject jsonObject : list) {
            StockRealDTO stockRealDTO = JSONObject.parseObject(jsonObject.toJSONString(), StockRealDTO.class);
            stockRealDTO.setSid(sid);
            result.add(stockRealDTO);
        }
        return result;
    }

    /**
     * 返回 date1 ---> date2 之间相差的时间天数
     * @param date1 时间1
     * @param date2 时间2
     * @return 相差的时间天数(date1< date2---> result < 0; date1 > date2 ---> result > 0)
     */
    private long getDifferDayTime(LocalDate date1, LocalDate date2) {
        Duration duration = Duration.between(date2.atStartOfDay(), date1.atStartOfDay());
        return duration.toDays();
    }
}
