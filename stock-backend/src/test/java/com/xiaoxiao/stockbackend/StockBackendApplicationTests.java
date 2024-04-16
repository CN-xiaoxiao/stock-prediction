package com.xiaoxiao.stockbackend;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.utils.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;

@SpringBootTest
class StockBackendApplicationTests {

    @Test
    void contextLoads() {
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
}
