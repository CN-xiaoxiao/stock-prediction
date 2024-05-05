package com.xiaoxiao.stockbackend.utils.net;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.response.DataTreatingResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/**
 * @ClassName DataTreatingUtils
 * @Description 与数据处理服务器端进行通信的网络工具类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Component
public class DataTreatingUtils {
    @Resource
    RestTemplate restTemplate;
    @Value("${spring.web.data-treating.url}")
    String url;
    @Value("${spring.web.data-treating.port}")
    String port;
    String address;

    @PostConstruct
    public void init() {
        this.address = url + ":" + port;
    }

    public DataTreatingResponse doGet(String url) {
        return this.doGet(url, this.address);
    }

    public DataTreatingResponse doGet(String url, String address) {
        String URL = address + "/treating" + url;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.GET, formEntity, String.class);
            return JSONObject.parseObject(exchange.getBody()).to(DataTreatingResponse.class);
        } catch (Exception e) {
            return DataTreatingResponse.errorResponse(e);
        }
    }

    public DataTreatingResponse doPost(String url, Object data, Map<String, String> paramMap) {
        String rawData = JSONObject.toJSONString(data);
        String URL = this.address + "/treating" + url;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (paramMap != null ) {
            StringBuilder sb = new StringBuilder(URL);
            sb.append("?");
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            URL = sb.toString();
        }

        RequestEntity requestEntity = new RequestEntity(rawData, headers, HttpMethod.POST, URI.create(URL));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        try {
            return JSONObject.parseObject(responseEntity.getBody()).to(DataTreatingResponse.class);
        } catch (Exception e) {
            return DataTreatingResponse.errorResponse(e);
        }
    }
}
