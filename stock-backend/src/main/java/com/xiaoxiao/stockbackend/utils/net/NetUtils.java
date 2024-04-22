package com.xiaoxiao.stockbackend.utils.net;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 网络请求工具，用于调用第三方API接口
 */
@Component
public class NetUtils {
    private static final Logger log = LoggerFactory.getLogger(NetUtils.class);
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${spring.web.tushare.token}")
    String tushareToken;

    @Value("${spring.web.tushare.url}")
    String url;

    ArrayList<String> tokens = new ArrayList<>();
    @Getter
    String token;

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

    public String updateToken(String token) {
        int i = tokens.indexOf(token);
        if (i == -1) throw new RuntimeException("token不存在");
        this.token = tokens.get((i+1)%tokens.size());
        return this.token;
    }

    @PostConstruct
    public void init() {
        String[] split = tushareToken.split(",");
        tokens.addAll(Arrays.asList(split));
        token = tokens.get(0);
    }

    /**
     * 快速创建第三方api接口的请求类
     * @param apiName api接口名称
     * @param token token
     * @param params 请求参数
     * @param fields 请求字段
     * @return 第三方api请求接口类
     */
    public StockApiVO createApiVO(String apiName, String token, Map<String, String> params, List<String> fields) {
        return new StockApiVO(apiName, token, params, fields);
    }
}
