package com.xiaoxiao.stockbackend.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import com.xiaoxiao.stockbackend.entity.dto.TreatingTokenDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.*;
import com.xiaoxiao.stockbackend.mapper.StockFavoriteMapper;
import com.xiaoxiao.stockbackend.mapper.TreatingTokenMapper;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.InfluxDBUtils;
import com.xiaoxiao.stockbackend.utils.net.DataTreatingUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName StockPredictServiceImpl
 * @Description 用来处理股票预测的服务类
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Service
public class StockPredictServiceImpl implements StockPredictService {
    @Resource
    StockDailyService stockDailyService;
    @Resource
    StockService stockService;
    @Resource
    TreatingTokenMapper treatingTokenMapper;
    @Resource
    StockFavoriteMapper stockFavoriteMapper;
    @Resource
    DataTreatingUtils dataTreatingUtils;
    @Resource
    InfluxDBUtils influxDBUtils;

    @Value("${spring.influx.measurements.predict}")
    String measurements;

    private String registerToken = this.generateNewToken();
    private final Map<String, TreatingTokenDTO> treatingTokenCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initTokenCache() {
        treatingTokenMapper.selectAllTokens()
                .forEach(v->treatingTokenCache.put(v.getToken(), v));
    }

    /**
     * 将 StockRealVO 转换为 String 数组，并除去多余项
     * @param vo StockRealVO 对象
     * @return String 数组
     */
    @Override
    public String[] ObjectToStringArr(StockRealVO vo) {
        Field[] fields = vo.getClass().getDeclaredFields();
        String[] values = new String[7];
        int count = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("preClose") ||
                    field.getName().equals("change") ||
                    field.getName().equals("amount") ||
                    field.getName().equals("pctChg")) {
                continue;
            }
            try {
                values[count++] = field.get(vo) + "";
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    /**
     * 生成数据处理服务器注册用的 token
     * @return 返回一个 token
     */
    @Override
    public String registerToken() {
        return registerToken;
    }

    @Override
    public boolean verifyAndRegister(String token) {
        if (this.registerToken.equals(token)) {
            TreatingTokenDTO dto = new TreatingTokenDTO(null, token);
            if (treatingTokenMapper.insertToken(dto)) {
                registerToken = this.generateNewToken();
                this.addTreatingCache(dto);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有用户收藏夹中的股票
     * @return 股票集合
     */
    @Override
    public List<String> trainingList() {
        List<Favorite> list = stockFavoriteMapper.queryAllUserFavorite();
        Set<String> set = new HashSet<>();
        for (Favorite favorite : list) {
            String favoriteList = favorite.getFavoriteList();
            List<String> strings;
            String[] split = favoriteList.split(",");//TODO .JSONException
            strings = Arrays.asList(split);
            set.addAll(strings);
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<StockPredictVO> getPredictData(List<StockRealVO> stockRealVOList, boolean flag) {
        if (stockRealVOList == null || stockRealVOList.isEmpty()) return null;

        StockHistoryVO stockHistoryVO = new StockHistoryVO();
        List<JSONObject> list = new ArrayList<>();
        stockRealVOList.forEach(v -> list.add(JSONObject.parseObject(JSONObject.toJSONString(v))));
        stockHistoryVO.setList(list);
        Map<String, String> map = null;
        if (flag) {
             map = Map.of("flag", "true");
        }
        try {
            DataTreatingResponse dataTreatingResponse =
                    dataTreatingUtils.doPost("/predict", stockHistoryVO, map);
            Object data = dataTreatingResponse.data();
            return JSONArray.parseArray(data.toString(), StockPredictVO.class);
        } catch (Exception e) {
            log.warn("请求失败");
            return null;
        }
    }

    @Override
    public List<StockPredictVO> predict(String tsCode) {
        LocalDate date =  LocalDate.now().plusMonths(-3);
        List<StockRealVO> stockRealDTOS = stockDailyService.getStockDailyHistory(tsCode, date);
        stockRealDTOS = stockRealDTOS.subList(stockRealDTOS.size() - 22, stockRealDTOS.size());
        return this.getPredictData(stockRealDTOS, false);
    }

    /**
     * 从数据库中获取预测的股票数据
     * @param tsCode ts股票代码
     * @param start 开始时间
     * @param end 结束时间
     * @return 预测数据集合
     */
    @Override
    public List<StockPreVO> getPredictList(String tsCode, LocalDate start, LocalDate end) {
        List<StockPreVO> stockPreVOList = new ArrayList<>();

        long sid = stockService.querySidByTsCode(tsCode);
        if (sid <= 0) return null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StockHistoryVO stockHistoryVO = influxDBUtils.
                readData(sid, measurements, dtf.format(start), dtf.format(end));

        List<JSONObject> list = stockHistoryVO.getList();
        for (JSONObject jsonObject : list) {
            StockPreVO stockPreVO = JSONObject.parseObject(jsonObject.toJSONString(), StockPreVO.class);
            stockPreVOList.add(stockPreVO);
        }

        return stockPreVOList;
    }

    /**
     * 生成token
     * @return 生成的token
     */
    private String generateNewToken() {
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(24);
        for (int i = 0; i < 24; i++)
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        return sb.toString();
    }

    private void addTreatingCache(TreatingTokenDTO dto) {
        treatingTokenCache.put(dto.getToken(), dto);
    }
}
