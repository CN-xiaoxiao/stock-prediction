package com.xiaoxiao.stockbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName DataServerController
 * @Description 与数据处理服务器进行交互
 * @Author xiaoxiao
 * @Version 1.0
 */
@RestController
@RequestMapping("/treating")
public class DataTreatingController {
    @Resource
    StockPredictService stockPredictService;

    @GetMapping("/register")
    public RestBean<Void> register(@RequestHeader("Authorization") String token) {
        return stockPredictService.verifyAndRegister(token) ?
                RestBean.success() : RestBean.failure(401, "客户端注册失败，请检查Token是否正确");
    }

//    @PostMapping("/test")
//    public RestBean<String> test(@RequestBody String json) {
//        return RestBean.success(json);
//    }
}
