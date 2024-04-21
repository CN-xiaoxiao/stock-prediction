package com.xiaoxiao.stockbackend;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.HotStockVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockHistoryVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
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
import java.util.*;

@SpringBootTest
class StockBackendApplicationTests {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    InfluxDBUtils influxDBUtils;

    @Test
    void contextLoads() {
    }

    @Test
    void testInfluxDbWrite() {
        StockRealVO stockRealVO =
                new StockRealVO("000001.SZ", "20240418",
                        10.58,
                        11.03,
                        10.56,
                        10.8,
                        10.62,
                        0.18,
                        1.6949,
                        3165914.26,
                        3427338.982);
        System.out.println(stockRealVO);
        influxDBUtils.writeRealData(stockRealVO);
    }

    @Test
    void testInfluxDbRead() {
        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(93707909304815616L, "-4d", null);
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
}
