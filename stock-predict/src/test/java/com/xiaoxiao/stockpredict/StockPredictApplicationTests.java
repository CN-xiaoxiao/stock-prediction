package com.xiaoxiao.stockpredict;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.Const;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;
import com.xiaoxiao.stockpredict.entity.vo.response.StockTestPredictVO;
import com.xiaoxiao.stockpredict.model.predictDemo.MaxAndMinArrayVO;
import com.xiaoxiao.stockpredict.service.StockPredictService;
import com.xiaoxiao.stockpredict.utils.InfluxDBUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName StockPredictApplicationTests
 * @Description 数据处理服务器端的测试类
 * @Author xiaoxiao
 * @Version 1.0
 */
@SpringBootTest
public class StockPredictApplicationTests {

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    StockPredictService stockPredictService;
    @Resource
    InfluxDBUtils influxDBUtils;

    @Test
    public void contextLoads() {}

    @Test
    public void testWrite() {
        double[] max = new double[]{24.91, 25.31, 24.52, 25.01, 5055284.6};
        double[] min = new double[]{8.6, 8.7, 8.45, 8.6, 299089.62};
        String stockCode = "000001.SZ";

        stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MIN + stockCode,
                JSONObject.toJSONString(min));
        stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MAX + stockCode,
                JSONObject.toJSONString(max));

    }

    @Test
    public void testRead() {
        String stockCode = "000001.SZ";
        String s1 = stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MIN + stockCode);
        String s2 = stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MAX + stockCode);

        double[] min = JSONObject.parseObject(s1, double[].class);
        double[] max = JSONObject.parseObject(s2, double[].class);
    }

    private static double[] jsonToDoubleArr(String json) {
        JSONArray jsonArray = JSONArray.parseArray(json);
        double[] result = new double[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.getDouble(i);
        }
        return result;
    }

    @Test
    public void testInfluxdbWrite() {
        StockTestPrice stockTestPrice = new StockTestPrice();
        stockTestPrice.setDate("20240504");
        stockTestPrice.setSymbol("000001.SZ");
        stockTestPrice.setOpen(123.5);
        stockTestPrice.setHigh(12.5);
        stockTestPrice.setLow(13.5);
        stockTestPrice.setClose(23.5);
        stockTestPrice.setVolume(13.5);
        influxDBUtils.writeTestData(stockTestPrice);
    }

    @Test
    public void testInfluxdbRead() {
        String stockCode = "000001.SZ";
        String startTime = "2023-01-01";
        String endTime = "2024-12-31";
        StockTestPredictVO stockTestPredictVO = influxDBUtils.readTestData(stockCode, startTime, endTime);
        System.out.println(stockTestPredictVO);
    }
    @Test
    public void testDeleteInfluxdb() {
        influxDBUtils.deleteTestData("000001.SZ");
    }

    @Test
    public void addMinAndMaxArrayToRedis() {
        String path = "D:\\code\\stock-prediction\\array";
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {
            Arrays.stream(files)
                    .filter(File::isFile)
                    .forEach(file -> {
                        try {
                            // 读取.json文件内容并转换为Java对象
                            String jsonContent = FileUtils.readFileToString(new File(path.concat("\\").concat(file.getName())), StandardCharsets.UTF_8);
                            MaxAndMinArrayVO maxAndMinArrayVO = JSONObject.parseObject(jsonContent, MaxAndMinArrayVO.class);

//                            System.out.println("maxAndMinArrayVO = " + maxAndMinArrayVO);

                            stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MIN + maxAndMinArrayVO.getSymbol(),
                                    JSONObject.toJSONString(maxAndMinArrayVO.getMinArray()));
                            stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MAX + maxAndMinArrayVO.getSymbol(),
                                    JSONObject.toJSONString(maxAndMinArrayVO.getMaxArray()));
                            stringRedisTemplate.opsForValue().set(Const.STOCK_MODEL + maxAndMinArrayVO.getSymbol(),
                                    maxAndMinArrayVO.getSymbol(), 30, TimeUnit.DAYS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            System.out.println("文件夹为空或不存在文件");
        }
    }
}
