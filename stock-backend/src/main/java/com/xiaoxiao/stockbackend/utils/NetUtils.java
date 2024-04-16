package com.xiaoxiao.stockbackend.utils;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 网络请求工具，用于调用第三方API接口
 */
@Component
public class NetUtils {
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${spring.web.tushare.token}")
    String tushareToken;

    @Value("${spring.web.tushare.url}")
    String url;

    /**
     * 向第三方股票数据API发起Post请求获取数据
     * @param vo 请求参数实体类
     * @return StockApiResponse 响应对象
     * @throws IOException 可能抛出的异常
     * @throws InterruptedException 可能抛出的异常
     */
    public StockApiResponse doPost(StockApiVO vo) throws IOException, InterruptedException {
        String rawData = JSONObject.toJSONString(vo);
        HttpRequest request =HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(rawData))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return JSONObject.parseObject(response.body(), StockApiResponse.class);
    }
}
