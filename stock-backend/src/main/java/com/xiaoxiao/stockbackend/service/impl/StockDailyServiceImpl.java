package com.xiaoxiao.stockbackend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import com.xiaoxiao.stockbackend.utils.net.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class StockDailyServiceImpl implements StockDailyService {

    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    StockService stockService;
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

    /**
     * 获取（更新）ts代码 的历史交易数据
     * @param tsCode 股票ts代码
     */
    @Override
    public void updateStockDailyHistory(String tsCode) {
        long sid = stockBasicsMapper.querySidByTsCode(tsCode);
        if (sid <= 0) return;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.plusMonths(-3);
        // 获取三个月的数据
        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(sid, dtf.format(startDate), dtf.format(endDate));
        LocalDate yesterday = endDate.plusDays(-1);
        // 历史数据为空
        if (stockHistoryVO == null || stockHistoryVO.getList().isEmpty()) { // 拉取2010年1月1号到昨天的数据

            Map<String, String> params = Map.of("ts_code", tsCode,
                    "start_date", "20100101", "end_date", dtf2.format(yesterday));
            StockApiVO apiVO = netUtils.createApiVO("daily", netUtils.getToken(), params, null);

            try {
                StockApiResponse stockApiResponse = netUtils.doPost(apiVO);
                ArrayList<StockRealVO> items = stockApiResponse.getItems(StockRealVO.class);
                if (items == null || items.isEmpty()) {
                    log.warn("获取ts_code={} 的历史数据失败", tsCode);
                    return;
                } else {
                    items.forEach(v->influxDBUtils.writeRealData(v));
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            List<JSONObject> list = stockHistoryVO.getList();
            String tradeDate = list.get(list.size() - 1).get("tradeDate").toString();
            LocalDate date = LocalDate.parse(tradeDate, dtf2);
            
            // 如果数据库中的最新的数据比昨天还早
            if (date.isBefore(yesterday)) {
                boolean flag = this.inStockMarketDay(date, yesterday);
                if (flag) return;
                Map<String, String> params = Map.of("ts_code", tsCode,
                        "start_date", dtf2.format(date.plusDays(1)), "end_date", dtf2.format(yesterday));
                StockApiVO apiVO = netUtils.createApiVO("daily", netUtils.getToken(), params, null);
                try {
                    ArrayList<StockRealVO> items = netUtils.doPost(apiVO).getItems(StockRealVO.class);
                    if (items != null && !items.isEmpty()) {
                        items.forEach(v->influxDBUtils.writeRealData(v));
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 判断从一个范围内的日期是否在非交易日
     * @param date 开始日期
     * @param yesterday 结束日期
     * @return true: 在非交易日; false: 不在非交易日(有一个或多个)
     */
    private boolean inStockMarketDay(LocalDate date, LocalDate yesterday) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = dtf.format(date);
        StockMarketDTO stockMarket = stockService.getStockMarket(format);
        if (stockMarket == null) throw new RuntimeException("没有 " + format +" 的休市日历");
        String marketData = stockMarket.getData();

        do {
            String temp = dtf2.format(date);
            if (!marketData.contains(temp)) return false;
            date = date.plusDays(1);
        } while (date.isBefore(yesterday));
        return true;
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
