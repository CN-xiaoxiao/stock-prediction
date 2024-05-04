package com.xiaoxiao.stockpredict.controller;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.Response;
import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.vo.request.StockDailyVO;
import com.xiaoxiao.stockpredict.entity.vo.request.StockHistoryVO;
import com.xiaoxiao.stockpredict.entity.vo.response.StockPredictPriceVO;
import com.xiaoxiao.stockpredict.service.StockPredictService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName PredictController
 * @Description 股票预测-数据处理服务器端接口
 * @Author xiaoxiao
 * @Version 1.0
 */
@RestController
@RequestMapping("/treating")
public class PredictController {

    @Resource
    StockPredictService stockPredictService;

    @GetMapping("/test")
    public String predict() {
        StockData sd = new StockData();
        sd.setClose(123.55);
        sd.setOpen(123.55666);
        return JSONObject.toJSONString(sd);
    }

    /**
     * 预测接口，调用此接口可进行股票预测
     * @param vo 一个股票历史教育数据的集合需要有以下内容: (id tradeDate tsCode open close high low vol)
     * @return 预测好的数据
     */
    @PostMapping("/predict")
    public Response doPredict(@RequestBody @Valid StockHistoryVO vo,
                              @RequestParam(value = "flag",required = false,defaultValue = "false") boolean flag) {
        List<StockPredictPriceVO> stockPredictPriceVOS = stockPredictService.doPredict(vo, flag);
        if (stockPredictPriceVOS!=null && !stockPredictPriceVOS.isEmpty()) {
            return Response.successResponse(stockPredictPriceVOS);
        }
        return Response.errorResponse("服务器内部错误");
    }

    @PostMapping("/train")
    public Response doTrain(@RequestBody @Valid StockHistoryVO vo) {
        stockPredictService.doTrain(vo);
        return Response.successResponse();
    }
}
