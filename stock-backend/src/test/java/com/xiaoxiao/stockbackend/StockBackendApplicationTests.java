package com.xiaoxiao.stockbackend;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.StockRealDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.HotStockVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import com.xiaoxiao.stockbackend.utils.ObjectUtils;
import com.xiaoxiao.stockbackend.utils.net.SpiderUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@SpringBootTest
class StockBackendApplicationTests {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    InfluxDBUtils influxDBUtils;
    @Resource
    StockDailyService stockDailyService;

    @Test
    void contextLoads() {
    }

    @Test
    void testInfluxDbWrite() {
        StockRealVO stockRealVO =
                new StockRealVO("000001.SZ", "20240417",
                        10.26,
                        10.63,
                        10.21,
                        10.62,
                        10.28,
                        0.34,
                        3.3074,
                        2232640.57,
                        2337576.587);
        System.out.println(stockRealVO);
        influxDBUtils.writeRealData(stockRealVO);
    }

    @Test
    void testInfluxDbRead() {
        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(93707909304815616L, "2024-04-16", "2024-04-19");
        System.out.println("size: " + stockHistoryVO.getList().size());
        System.out.println("stockHistoryVO = " + stockHistoryVO);
    }

    @Test
    public void testStockApi() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String tushareToken = "02bc5e5f1d7b4dbd77db7f84bd561074ea03accf39fc6fabe1ddd9c6";
        String url = "http://api.tushare.pro";

        StockApiVO vo = new StockApiVO();
        vo.setToken(tushareToken);
        vo.setApi_name("daily");
        Map<String, String> params = Map.of("ts_code", "000001.SZ", "start_date", "20230721", "end_date", "20240416");
        vo.setParams(params);

        String rawData = JSONObject.toJSONString(vo);
        HttpRequest request =HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(rawData))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        StockApiResponse stockApiResponse = JSONObject.parseObject(response.body(), StockApiResponse.class);
        System.out.println(stockApiResponse.getFields());
        ArrayList<StockRealVO> items = stockApiResponse.getItems(StockRealVO.class);

        for (int i = 0; i < 10; i++) {
            if (items != null) {
                System.out.println(items.get(i));
            }
        }
//        ArrayList<JSONArray> items = stockApiResponse.getItems();
//        for (int i = 0; i < 10; i++) {
//            JSONArray o = null;
//            if (items != null) {
//                o = (JSONArray)items.get(i);
//            }
//            Object[] array = null;
//            if (o != null) {
//                array = o.stream().map((v)-> {
//                    if (v instanceof BigDecimal) {
//                        return ((BigDecimal) v).doubleValue();
//                    } else {
//                        return v;
//                    }
//                }).toArray();
//            }
//
//            ObjectUtils utils = new ObjectUtils();
//            StockRealVO stockRealVO = utils.objectArrayToObject(array, StockRealVO.class);
//            System.out.println(stockRealVO);
//        }
    }

    @Test
    public void testSplit() {
        String s = "123";
        String[] split = s.split(",");
        for (String string : split) {
            System.out.println(string);
        }
    }

    @Test
    public void testSpider() throws IOException, InterruptedException {
        SpiderUtils spiderUtils = new SpiderUtils();
//        List<HotStockVO> hotStock = spiderUtils.getHotStock(1, 20);
//        for (HotStockVO hotStockVO : hotStock) {
//            System.out.println(hotStockVO);
//        }
        List<String> tradingDay = spiderUtils.getTradingDay("2024-03");
        if (tradingDay != null && !tradingDay.isEmpty()) {
            for (String s : tradingDay) {
                System.out.println(s);
            }
        }
    }

    @Test
    public void testRedis() throws IOException, InterruptedException {
        String s = stringRedisTemplate.opsForValue().get("jwt:blacklist:d8967956-1b53-47fe-8a30-c25bac4bf406");
        System.out.println(s+"13");
    }

    @Test
    public void testDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY,1);
        String format = sdf.format(calendar.getTime());
        System.out.println(format);
    }

    @Test
    public void testGetDifferDay() {
        LocalDate localDate = LocalDate.parse("2024-04-20");
        LocalDate date2 = LocalDate.parse("2024-04-22");
        System.out.println("localDate = " + localDate);
        System.out.println("date2 = " + date2);
        System.out.println("date2 - date1 = " + calculateDaysBetween(localDate, date2));

    }

    private static long calculateDaysBetween(LocalDate date1, LocalDate date2) {
        Duration between = Duration.between(date1.atStartOfDay(), date2.atStartOfDay());
        return between.toDays();
    }

    @Test
    public void testGetStockDailyHistory() {
        List<StockRealDTO> stockDailyHistory = stockDailyService
                .getStockDailyHistory("000001.SZ", LocalDate.parse("2024-04-19"));
        Instant instant = stockDailyHistory.get(0).getTradeDate();

        LocalDate date = LocalDate.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
        System.out.println("date = " + date);
        stockDailyHistory.forEach(System.out::println);
    }
}
