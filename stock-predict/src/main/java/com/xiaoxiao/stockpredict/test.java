package com.xiaoxiao.stockpredict;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.ConnectionConfig;
import com.xiaoxiao.stockpredict.entity.Const;
import com.xiaoxiao.stockpredict.entity.Response;
import com.xiaoxiao.stockpredict.entity.StockData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName test
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
public class test {
    public static void main(String[] args) {
//        testOkHttpGet();
//        StockData sd = new StockData();
//        sd.setClose(123.55);
//        sd.setOpen(123.55666);
//        com.xiaoxiao.stockpredict.entity.Response response = testOkHttpPost("/test", sd);
//        System.out.println("response = " + response);
//        ConnectionConfig connectionConfig = readConfiguration();
//        System.out.println("connectionConfig = " + connectionConfig);

//        updateMinAndMaxArray();
        testJSONToList();
    }

    public static void testOkHttpGet() {
        String url = "http://localhost:8080/api/stock/register";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(headers);
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoieGlhb3hpYW8iLCJpZCI6NCwiZXhwIjoxNzE1MTUxOTcxLCJpYXQiOjE3MTQ1NDcxNzEsImp0aSI6IjZjOTRmNGE2LWQ5ZGItNGNiMi1iNDZkLWVjOTFjMjY0NTE5MiIsImF1dGhvcml0aWVzIjpbIlJPTEVfdXNlciJdfQ.a2frHLewhskoOE9-JcA37F8EF0Qe6mbmAQeGm7Q7o98");

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, formEntity, String.class);
        System.out.println("exchange.getBody() = " + exchange.getBody());

    }
    public static com.xiaoxiao.stockpredict.entity.Response testOkHttpPost(String url, Object data) {
        RestTemplate restTemplate = new RestTemplate();
        String rawData = JSONObject.from(data).toJSONString();
        String URL = "http://localhost:8080" + "/treating" + url;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity requestEntity = new RequestEntity(rawData, headers, HttpMethod.POST, URI.create(URL));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        try {
            return JSONObject.parseObject(responseEntity.getBody()).to(com.xiaoxiao.stockpredict.entity.Response.class);
        } catch (Exception e) {
            return Response.errorResponse(e);
        }
    }

    public static ConnectionConfig readConfiguration() {
        File configurationFile = new File("config/server.json");

        if (configurationFile.exists()) {
            try (FileInputStream stream = new FileInputStream(configurationFile)){
                byte[] bytes = new byte[stream.available()];
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = stream.read(bytes)) != -1){
                    sb.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
                }
                String raw = sb.toString();
                return JSONObject.parseObject(raw).to(ConnectionConfig.class);
            } catch (IOException e) {
//                log.error("读取配置文件时出错", e);
            }
        }
        return null;
    }

    public static void updateMinAndMaxArray() {

    }

    public static void testJSONToList() {

        String JSON = "[123,456,789]";
        List<String> strings = JSONArray.parseArray(JSON, String.class);

        Set<String> set = new HashSet<>(strings);
        set.addAll(strings);
        System.out.println(set);
    }
}
