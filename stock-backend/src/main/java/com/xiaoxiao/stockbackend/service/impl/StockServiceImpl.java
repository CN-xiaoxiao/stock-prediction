package com.xiaoxiao.stockbackend.service.impl;

import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.NetUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StockServiceImpl implements StockService {

    @Value("${spring.web.tushare.token}")
    String tushareToken;
    @Resource
    NetUtils netUtils;

    @Override
    public StockRealVO getDailyStockData(String tsCode, Date startDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(startDate);

        // TODO 股票会出现休市
        Map<String, String> params = Map.of("ts_code", tsCode, "start_date", date, "end_date", date);

        StockApiVO vo = createApiVO("daily", tushareToken, params, null);
        StockApiResponse response;

        try {
            response = netUtils.doPost(vo);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Objects.requireNonNull(response.getItems(StockRealVO.class)).get(0);
    }

    private StockApiVO createApiVO(String apiName, String token, Map<String, String> params, List<String> fields) {
        return new StockApiVO(apiName, token, params, fields);
    }
}
