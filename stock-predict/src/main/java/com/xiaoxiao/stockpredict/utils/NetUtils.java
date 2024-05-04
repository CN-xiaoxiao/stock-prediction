package com.xiaoxiao.stockpredict.utils;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.ConnectionConfig;
import com.xiaoxiao.stockpredict.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;

/**
 * @ClassName NetUtils
 * @Description 网络通信工具类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class NetUtils {

    @Resource
    RestTemplate restTemplate;
    @Lazy
    @Resource
    ConnectionConfig config;

    public boolean registerToServer(String address, String token) {
        log.info("正在向服务端注册，请稍后...");
        Response response = this.doGet("/register", address, token);

        if (response.success()) {
            log.info("客户端注册已完成");
        } else {
            log.error("客户端注册失败：{}", response.getMessage());
        }

        return response.success();
    }

    private Response doGet(String url) {
        return this.doGet(url, config.getAddress(), config.getToken());
    }

    private Response doGet(String url, String address, String token) {
        String URL = address + "/treating" + url;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.GET, formEntity, String.class);
            return JSONObject.parseObject(exchange.getBody()).to(Response.class);
        } catch (Exception e) {
            return Response.errorResponse(e);
        }
    }

    private Response doPost(String url, Object data) {
        String rawData = JSONObject.from(data).toJSONString();
        String URL = config.getAddress() + "/treating" + url;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity requestEntity = new RequestEntity(rawData, headers, HttpMethod.POST, URI.create(URL));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        try {
            return JSONObject.parseObject(responseEntity.getBody()).to(Response.class);
        } catch (Exception e) {
            return Response.errorResponse(e);
        }
    }
}
