package com.xiaoxiao.stockbackend.utils.net;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.response.HotStockVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 爬虫工具
 */
@Slf4j
@Component
public class SpiderUtils {

    /**
     * 从 “东方财富网” 爬取热门榜单
     * @param pageNum 页号
     * @param pageSize 页容量
     * @return 热门股票的实体类集合（HotStockVO）
     * @throws IOException 可能抛出的异常
     * @throws InterruptedException 可能抛出的异常
     */
    public List<HotStockVO> getHotStock(int pageNum, int pageSize) throws IOException, InterruptedException {
        Map<String, String> prams = getPrams(pageNum, pageSize);

        HttpClient client = HttpClient.newHttpClient();
        String baseUrl = "https://69.push2.eastmoney.com/api/qt/clist/get";
        String url = buildUrlWithParams(baseUrl, prams);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0")
                .header("Referer", "https://quote.eastmoney.com/center/gridlist.html")
                .header("Content-Type", "application/javascript; charset=UTF-8")
                .build();
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = send.body();

        return this.getHotStockData(body);
    }

    /**
     * 从深圳证券交易所爬取交易日历
     * @param date 时间 格式如下: 2024-03 或者 2024-10
     * @return 返回一个不是交易日的字符串集合。
     */
    public List<String> getTradingDay(String date) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Random random = new Random();
        Map<String, String> prams = Map.of("month", date, "random", String.valueOf(random.nextDouble()));
        String baseUrl = "http://www.szse.cn/api/report/exchange/onepersistenthour/monthList";
        String url = this.buildUrlWithParams(baseUrl, prams);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                .header("Referer", "https://www.szse.cn/aboutus/calendar/")
                .header("Content-Type", "application/javascript; charset=UTF-8")
                .build();
        client.sslParameters();
        log.info("向 {} 发起网络请求", baseUrl);
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(send);
        return send.statusCode() == 200 ? this.getTradingDayData(send.body()) : null;
    }

    /**
     * 拼接请求参数
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 请求参数集合
     */
    private Map<String, String> getPrams(int pageNum, int pageSize) {
        Map<String, String> p = Map.of(
                "cb", "jQuery11240438573760666664_1713516766666",
                "pn", String.valueOf(pageNum),
                "pz",String.valueOf(pageSize),
                "po","1",
                "np","1",
                "ut","bd1d9ddb04089700cf9c27f6f7426281",
                "fltt","2",
                "invt","2",
                "wbp2u","|0|0|0|web"
        );
        Map<String, String> prams = new HashMap<>(p);
        prams.put("fid","f3");
        prams.put("fs","m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23,m:0+t:81+s:2048");
        prams.put("fields","f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152");
        prams.put("_","1713516766670");
        return prams;
    }

    /**
     * 拼接URI
     * @param baseUrl 基地址
     * @param queryParams 请求参数
     * @return 请求URI
     */
    private String buildUrlWithParams(String baseUrl, Map<String, String> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        queryParams.forEach((key, value) -> {
            urlBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            urlBuilder.append("&");
        });

        // 移除最后一个"&"
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);

        return urlBuilder.toString();
    }

    /**
     * 转换爬取到的数据到实体类中
     * @param connect 爬取到的数据集
     * @return 热门股票的实体类集合（HotStockVO）
     */
    private List<HotStockVO> getHotStockData(String connect) {

        List<HotStockVO> list = new ArrayList<HotStockVO>();

        connect = connect.split("\"data\":")[1];
        connect = connect.substring(0, connect.lastIndexOf("}"));
        JSONObject jsonObject = JSONObject.parseObject(connect);
        JSONArray jsonArray = jsonObject.getJSONArray("diff");

        for (Object object : jsonArray) {
            HotStockVO stock = new HotStockVO();
            JSONObject jsonObject1 = JSONObject.parseObject(object.toString());
            stock.setTsCode(jsonObject1.get("f12").toString());
            stock.setName(jsonObject1.getString("f14"));
            list.add(stock);
        }

        return list;
    }

    private List<String> getTradingDayData(String data) {
        List<String> list = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        jsonArray.forEach(item -> {
            JSONObject jsonObject1 = JSONObject.parseObject(item.toString());
            Object o = jsonObject1.get("jybz");
            if (o != null && o.toString().equals("0")) {
                list.add(jsonObject1.get("jyrq").toString());
            }
        });
        return list;
    }
}
