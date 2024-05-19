package com.xiaoxiao.stockbackend;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.csvreader.CsvWriter;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockPredictDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.*;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.mapper.StockFavoriteMapper;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import com.xiaoxiao.stockbackend.utils.net.DataTreatingUtils;
import com.xiaoxiao.stockbackend.utils.net.SpiderUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@SpringBootTest
class StockBackendApplicationTests {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    InfluxDBUtils influxDBUtils;
    @Resource
    StockDailyService stockDailyService;
    @Resource
    StockService stockService;
    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    StockFavoriteMapper stockFavoriteMapper;
    @Resource
    StockPredictService stockPredictService;
    @Resource
    DataTreatingUtils dataTreatingUtils;


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
        StockHistoryVO stockHistoryVO = influxDBUtils.readRealData(93707909304815616L,
                "2024-04-16", "2024-05-01");
        System.out.println("size: " + stockHistoryVO.getList().size());
        stockHistoryVO.getList().forEach(System.out::println);
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
        String s = "123,456,789";
        String[] split = s.split(",");
        List<String> list = List.of(split);
        list.forEach(System.out::println);
    }

    @Test
    public void testSpider() throws IOException, InterruptedException {
        SpiderUtils spiderUtils = new SpiderUtils();
//        List<HotStockVO> hotStock = spiderUtils.getHotStock(1, 20);
//        for (HotStockVO hotStockVO : hotStock) {
//            System.out.println(hotStockVO);
//        }

        String date = "2021-12";

        StockMarketDTO stockMarket = stockService.getStockMarket(date);
        if (stockMarket != null) {return;}

        List<String> tradingDay = spiderUtils.getTradingDay(date);
        if (tradingDay != null && !tradingDay.isEmpty()) {
            String jsonString = JSONObject.toJSONString(tradingDay);
            stockService.saveStockMarket(date, jsonString);
        }

//        for (int i = 1; i < 10; i++) {
//            String date = "2021-0";
//            date = date + i;
//
//            StockMarketDTO stockMarket = stockService.getStockMarket(date);
//            if (stockMarket != null) {return;}
//
//            List<String> tradingDay = spiderUtils.getTradingDay(date);
//            if (tradingDay != null && !tradingDay.isEmpty()) {
//                String jsonString = JSONObject.toJSONString(tradingDay);
//                stockService.saveStockMarket(date, jsonString);
//            }
//        }
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
        List<StockRealVO> stockDailyHistory = stockDailyService
                .getStockDailyHistory("000001.SZ", LocalDate.parse("2024-04-19"), LocalDate.parse("2024-04-23"));
//        Instant instant = stockDailyHistory.get(0).getTradeDate();

//        LocalDate date = LocalDate.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
//        System.out.println("date = " + date);
        stockDailyHistory.forEach(System.out::println);
    }

    @Test
    public void testUpdateStockDailyHistory() {
        stockDailyService.updateStockDailyHistory("000001.SZ");
    }

    @Test
    public void testGetMonth() {
        LocalDate date = LocalDate.now();
        System.out.println("date = " + date);
        System.out.println("date.getMonth() = " + date.getMonth());
        System.out.println("date.getYear() = " + date.getYear());
    }

    @Test
    public void testGetStockBasicsDTO() {
        List<StockBasicsDTO> stockBasicsDTO = stockService.getStockBasicsDTO("000001.SZ");
        stockBasicsDTO.forEach(System.out::println);
    }

    @Test
    public void testGetStockBasicsVO() {
//        List<StockBasicsVO> stockBasicsVO = stockService.getStockBasicsVO(1, 20, "0000");
//        stockBasicsVO.forEach(System.out::println);
    }

    @Test
    public void testRuntim() {
        try {
            t();
        } catch (RuntimeException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }
        System.out.println(123);
    }

    private void t() {
        throw new RuntimeException("testt");
    }

    @Test
    public void testQueryFavoriteByUserId() {
        FavoriteVO favoriteVO = stockService.queryFavoriteByUid(4);
        System.out.println(favoriteVO);
    }

    @Test
    public void testInsertFavorite() {
        Favorite favorite = new Favorite();
        favorite.setUid(1);
        favorite.setFavoriteList("123,456,789");
        boolean b = stockFavoriteMapper.updateFavorite(favorite);
        System.out.println(favorite);
    }

    @Test
    public void testJavaCSVWrite() {
        List<String> stockTsCode = stockService.getStockTsCode(1, 100);
        for (String s : stockTsCode) {
            log.info("正在保存股票[{}]的数据...", s);
            this.doCSVWrite(s);
        }
        log.info("共保存[{}]条数据", stockTsCode.size());
    }

    private void doCSVWrite(String tsCode) {
        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory(tsCode,
                LocalDate.parse("2010-01-01"),
                LocalDate.parse("2024-05-05")); // 3000多条数据
        CsvWriter csvWriter = new CsvWriter("src/main/resources/data/".concat(tsCode).concat(".csv"), ',', StandardCharsets.UTF_8);
        String[] headers = {"date", "symbol", "open", "close", "low", "high", "volume"};
        try {
            csvWriter.writeRecord(headers);
            for (StockRealVO stockRealVO : stockDailyHistory) {
                csvWriter.writeRecord(this.ObjectToStringArray(stockRealVO));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        csvWriter.close();
    }

    private <T> String[] ObjectToStringArray(T t) {
        Field[] fields = t.getClass().getDeclaredFields();
        String[] values = new String[7];
        int count = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("preClose") ||
                    field.getName().equals("change") ||
                    field.getName().equals("amount") ||
                    field.getName().equals("pctChg")) {
                continue;
            }
            try {
                values[count++] = field.get(t) + "";
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    @Test
    public void testObjectToStringArr() {
        StockRealVO vo = new StockRealVO("000001.SZ",
                "20240430",
                10.8,
                10.88,
                10.73,
                10.79,
                10.81,
                -0.02,
                -0.185,
                1324556.53,
                1431407.709);

        Field[] fields = vo.getClass().getDeclaredFields();
        String[] values = new String[7];
        int count = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("preClose") ||
                    field.getName().equals("change") ||
                    field.getName().equals("amount") ||
                    field.getName().equals("pctChg")) {
                continue;
            }
            try {
                values[count++] = field.get(vo) + "";
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        for (String value : values) {
            System.out.println(value);
        }
    }

    @Test
    public void testPredictServerPost() {
        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory("000001.SZ",
                LocalDate.parse("2016-01-01"),
                LocalDate.parse("2024-03-01"));

        String jsonString = JSONObject.toJSONString(stockDailyHistory);
        System.out.println(jsonString);
    }

    @Test
    public void testTrainingList() {
        List<String> strings = stockPredictService.trainingList();
        strings.forEach(System.out::println);
    }

    @Test
    public void testDataTreatingGet() {
        DataTreatingResponse dataTreatingResponse = dataTreatingUtils.doGet("/test");
        System.out.println(dataTreatingResponse);
    }

    @Test
    public void testDataTreatingPost() {
        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory("000001.SZ",
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-03-01"));
        StockHistoryVO stockHistoryVO = new StockHistoryVO();
        List<JSONObject> list = new ArrayList<>();
        stockDailyHistory.forEach(v -> {
            list.add(JSONObject.parseObject(JSONObject.toJSONString(v)));
        });
        stockHistoryVO.setList(list);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("flag", "true");
        DataTreatingResponse dataTreatingResponse = dataTreatingUtils.doPost("/predict", stockHistoryVO, paramMap);
        Object data = dataTreatingResponse.data();
        List<StockPredictVO> stockPredictVOS = JSONArray.parseArray(data.toString(), StockPredictVO.class);
        stockPredictVOS.forEach(System.out::println);
    }

    @Test
    public void testListSubList() {
        List list = List.of(123,456,789,123,444,55);
        list.forEach(System.out::println);
        list = list.subList(list.size() - 3, list.size());
        list.forEach(System.out::println);
    }

    @Test
    public void testJSONObjectToSet() {
        Set<String> set = new HashSet<>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.forEach(System.out::println);
        String jsonString = JSONObject.toJSONString(set);
        System.out.println("jsonString = " + jsonString);

        List<String> strings = JSONArray.parseArray(jsonString, String.class);
        System.out.println("strings = " + strings);
    }

    @Test
    public void testStockPredictMore() {
        String tsCode = "000001.SZ";
        LocalDate date =  LocalDate.now().plusMonths(-3);
        List<StockRealVO> stockRealDTOS = stockDailyService.getStockDailyHistory(tsCode, date);
        stockRealDTOS = stockRealDTOS.subList(stockRealDTOS.size() - 22 - 30, stockRealDTOS.size());

        List<StockPredictVO> predictData = stockPredictService.getPredictData(stockRealDTOS, true);
        log.info("正在将股票代码为[{}]的数据保存到数据库...", tsCode);
        log.info("共有[{}]条预测数据", predictData.size());
//        List<StockPredictDTO> predictDTOS = new ArrayList<>();
        for (StockPredictVO predictVO : predictData) {
            StockPredictDTO stockPredictDTO = new StockPredictDTO();

            long sid = stockService.querySidByTsCode(tsCode);
            stockPredictDTO.setSid(sid);

            String date1 = predictVO.getDate();
            Instant instant = getTrulyPredictDate(date1);
            stockPredictDTO.setTradeDate(instant);

            stockPredictDTO.setTsCode(predictVO.getSymbol());
            stockPredictDTO.setOpen(predictVO.getOpen());
            stockPredictDTO.setClose(predictVO.getClose());
            stockPredictDTO.setHigh(predictVO.getHigh());
            stockPredictDTO.setLow(predictVO.getLow());
            stockPredictDTO.setVol(predictVO.getVolume());
            influxDBUtils.writePredictData(stockPredictDTO);
//            predictDTOS.add(stockPredictDTO);
        }
//        StockPredictDTO stockPredictDTO = predictDTOS.get(predictDTOS.size() - 1);
//        System.out.println("stockPredictDTO = " + stockPredictDTO);
        log.info("股票代码为[{}]的股票的预测任务完成", tsCode);
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

    @Test
    public void testDeleteInfluxdbData() {
        long sid = stockService.querySidByTsCode("000001.SZ");
        influxDBUtils.deleteData("predict", sid,
                OffsetDateTime.parse("2010-01-10T00:00:00+08:00"), OffsetDateTime.now());
    }
}
