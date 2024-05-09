package com.xiaoxiao.stockbackend.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.dto.Account;
import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.dto.StockMarketDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.FavoriteVO;
import com.xiaoxiao.stockbackend.entity.vo.response.NewStockVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockBasicsVO;
import com.xiaoxiao.stockbackend.mapper.AccountMapper;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.mapper.StockFavoriteMapper;
import com.xiaoxiao.stockbackend.mapper.StockMarketMapper;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.Const;
import com.xiaoxiao.stockbackend.utils.JwtUtils;
import com.xiaoxiao.stockbackend.utils.net.NetUtils;
import com.xiaoxiao.stockbackend.utils.SnowflakeIdGenerator;
import com.xiaoxiao.stockbackend.utils.net.SpiderUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 股票基础数据的服务类
 */
@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Resource
    NetUtils netUtils;
    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    StockMarketMapper stockMarketMapper;
    @Resource
    AccountMapper accountMapper;
    @Resource
    StockFavoriteMapper stockFavoriteMapper;
    @Resource
    SnowflakeIdGenerator idGenerator;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    SpiderUtils spiderUtils;
    @Resource
    JwtUtils jwtUtils;


    /**
     * 分页查询股票基础数据
     * @param pageNum 页号
     * @param pageSize 查询的个数
     * @return
     */
    @Override
    public PageInfo<StockBasicsDTO> selectAllBasicsStockDataS(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<StockBasicsDTO> list = stockBasicsMapper.queryStockBasicsData();
        return new PageInfo<>(list);
    }

    /**
     * 分页查询股票基础数据
     * @param pageNum 页号
     * @param pageSize 查询的个数
     * @return
     */
    @Override
    public List<StockBasicsDTO> selectAllBasicsStockData(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return stockBasicsMapper.queryStockBasicsData();
    }


    /**
     * 保存（更新）股票基础数据
     * @return
     */
    @Override
    public String saveStockBasics() {
        int count = this.queryStockBasicsCount();
        if (count <= 0) {
            Map<String, String> params = Map.of("list_status","L");
            StockApiVO vo = netUtils.createApiVO("stock_basic", netUtils.getToken(), params, null);

            try(SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
                int times = 3;
                StockApiResponse stockApiResponse;
                // 失败重试，使用另外的token
                do {
                     stockApiResponse = netUtils.doPost(vo);
                    if (stockApiResponse.data() == null) {
                        netUtils.updateToken(netUtils.getToken());
                    } else {
                        break;
                    }
                } while (--times > 0);

                ArrayList<StockBasicsVO> items = stockApiResponse.getItems(StockBasicsVO.class);

                List<StockBasicsDTO> dtos = null;
                if (items != null) {
                    log.info("股票数量为：{}", items.size());
                    dtos = items.stream().map(item -> {
                        StockBasicsDTO dto = new StockBasicsDTO();
                        dto.setSid(idGenerator.nextId());
                        BeanUtils.copyProperties(item, dto);
                        return dto;
                    }).toList();
                }

                StockBasicsMapper mapper = sqlSession.getMapper(StockBasicsMapper.class);
                long millis = System.currentTimeMillis();
                log.info("正在保存数据 : {}", millis);
                if (dtos != null) {
                    dtos.forEach(mapper::insertStockBasics);
                    sqlSession.commit();
                }

                log.info("保存数据完成 : 耗时{}秒", (System.currentTimeMillis() - millis) / 1000.0);
            } catch (IOException | InterruptedException e) {
                return "数据获取失败";
            }
        } else {
            if (doDelintingAndNewStockBasics()) return null;
        }
        return null;
    }

    /**
     * 根据ts_code来查询股票基础信息
     * @param tsCode 股票ts代码
     * @return StockBasicsDTO 股票基础信息实体类集合
     */
    @Override
    public List<StockBasicsDTO> getStockBasicsDTO(String tsCode) {
        return stockBasicsMapper.fuzzyQueryStockBasicsDTOByTsCode(tsCode);
    }

    @Override
    public PageInfo<StockBasicsVO> getStockBasicsVO(int pageNum, int pageSize, String tsCode) {
        PageHelper.startPage(pageNum, pageSize);
        List<StockBasicsDTO> list = stockBasicsMapper.fuzzyQueryStockBasicsDTOByTsCode(tsCode);
        if (list == null || list.isEmpty()) return null;
        List<StockBasicsVO> vos = new ArrayList<>();
        for (StockBasicsDTO dto : list) {
            StockBasicsVO vo = new StockBasicsVO();
            BeanUtils.copyProperties(dto, vo);
            vos.add(vo);
        }
        return new PageInfo<>(vos);
    }


    @Override
    public int queryStockBasicsCount() {
        return stockBasicsMapper.countStockBasics();
    }

    /**
     * 保存股票交易日
     * @param date 月份 YYYY-MM
     * @param data 该月的股票交易日数据
     * @return 成功: ture, 失败: false
     */
    @Override
    public boolean saveStockMarket(String date, String data) {
        try {
            JSONObject.parseObject(data, new TypeReference<ArrayList<String>>(){});
            StockMarketDTO dto = stockMarketMapper.queryStockMarket(date);

            if (dto != null) {
                return true;
            }
            return stockMarketMapper.saveStockMarket(date, data);
        } catch (Exception e) {
            log.warn("保存股票交易信息出错: {}", e.getMessage());
            log.warn("{}", date);
            return false;
        }
    }

    @Override
    public boolean saveStockMarket(String date) {

        List<String> tradingDay = null;
        try {
            tradingDay = spiderUtils.getTradingDay(date);
        } catch (IOException | InterruptedException e) {
            log.warn("获取股票交易日历失败: {}", e.getMessage());
        }

        if (tradingDay != null && !tradingDay.isEmpty()) {
            String jsonString = JSONObject.toJSONString(tradingDay);
            this.saveStockMarket(date, jsonString);
        }

        return true;
    }

    /**
     * 获取股票交易日历
     * @param date 年月份 yyyy-MM
     * @return 休市日历
     */
    @Override
    public StockMarketDTO getStockMarket(String date) {
        return stockMarketMapper.queryStockMarket(date);
    }

    /**
     * 获取股票代码的 tsCode
     * @param pageNum 开始页码
     * @param pageSize 页容量
     * @return tsCode字符串集合
     */
    @Override
    public List<String> getStockTsCode(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return stockBasicsMapper.queryTsCode();
    }

    /**
     * 获取股票代码的 tsCode
     * @return tsCode字符串集合
     */
    @Override
    public List<String> getStockTsCode() {
        return stockBasicsMapper.queryTsCode();
    }

    /**
     * 根据 tsCode 查询股票的sid
     * @param tsCode tsCode
     * @return 返回 tsCode 股票的sid
     */
    @Override
    public long querySidByTsCode(String tsCode) {
        return stockBasicsMapper.querySidByTsCode(tsCode);
    }

    /**
     * 获取热门股票榜单
     * @return
     */
    @Override
    public List<StockBasicsVO> getHotStockData() {
        List<StockBasicsVO> list = new ArrayList<>(20);
        for (int i = 1; i <= 20; i++) {
            String s = stringRedisTemplate.opsForValue().get(Const.STOCK_HOT_LIST + i + ":");
            StockBasicsDTO stockBasicsDTO = JSONObject.parseObject(s, StockBasicsDTO.class);
            if (stockBasicsDTO == null) continue;
            StockBasicsVO stockBasicsVO = new StockBasicsVO();
            BeanUtils.copyProperties(stockBasicsDTO, stockBasicsVO);
            list.add(stockBasicsVO);
        }
        return list;
    }

    @Override
    public FavoriteVO queryFavoriteByUid(int uid) {
        if (!isAccount(uid)) return null;
        FavoriteVO vo = new FavoriteVO();
        Favorite favorite = stockFavoriteMapper.queryFavoriteByUserID(uid);
        BeanUtils.copyProperties(favorite, vo);
        List<String> list = List.of(favorite.getFavoriteList().split(","));
        vo.setFavoriteList(list);
        return vo;
    }

    @Override
    public boolean updateFavorite(Favorite favorite) {
        int uid = favorite.getUid();
        if (!isAccount(uid)) return false;
        if (stockFavoriteMapper.queryFavoriteByUserID(uid) == null) {
            stockFavoriteMapper.insertFavorite(favorite);
        }
        return stockFavoriteMapper.updateFavorite(favorite);
    }

    @Override
    public boolean insertFavorite(Favorite favorite) {
        int uid = favorite.getUid();
        if (!isAccount(uid)) return false;
        Favorite favorite1 = stockFavoriteMapper.queryFavoriteByUserID(uid);
        if (favorite1 != null) return false;    // 已经有数据
        return stockFavoriteMapper.insertFavorite(favorite);
    }

    @Override
    public boolean addFavorite(String tsCode, String token) {
        int id = jwtUtils.getId(token);
        if (!isAccount(id)) return false;
        Favorite favorite = stockFavoriteMapper.queryFavoriteByUserID(id);
        if (favorite == null) return false;

        String favoriteList = favorite.getFavoriteList();
        String[] split = favoriteList.split(",");
        Set<String> tsCodeSet = new HashSet<>(List.of(split));
        tsCodeSet.add(tsCode);
        StringBuilder sb = new StringBuilder();
        tsCodeSet.forEach(v -> sb.append(v).append(","));
        sb.deleteCharAt(sb.length() - 1);
        favorite.setFavoriteList(sb.toString());

        return stockFavoriteMapper.updateFavorite(favorite);
    }

    @Override
    public List<StockBasicsVO> getStockBasicsListForFavorite(int id) {
        FavoriteVO favoriteVO = this.queryFavoriteByUid(id);
        List<String> favoriteList = favoriteVO.getFavoriteList();

        List<StockBasicsVO> result = new ArrayList<>(favoriteList.size());
        for (String s : favoriteList) {
            StockBasicsDTO dto = stockBasicsMapper.selectStockBasicsByTsCode(s);
            StockBasicsVO stockBasicsVO = new StockBasicsVO();
            BeanUtils.copyProperties(dto, stockBasicsVO);
            result.add(stockBasicsVO);
        }
        return result;
    }

    /**
     * 判断是否操作 uid 的账户
     * @param uid 用户 id
     * @return true: 存在；false: 不存在
     */
    private boolean isAccount(int uid) {
        Account accountByUid = accountMapper.findAccountByUid(uid);
        return accountByUid != null;
    }

    // 判断是否退市，第三方接口没有，只能爬虫爬取。
    private List<String> isDelisting() {
        return null;
    }

    /**
     * 查询并获取新上市的股票
     * @return 新上市的 StockBasicsVO 股票集合
     */
    private List<StockBasicsVO> getNewStockBasicsData() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());
        Map<String, String> params = Map.of("start_date",date, "end_date", date);
        StockApiVO apiVO = netUtils.createApiVO("new_share", netUtils.getToken(), params, null);

        try {
            StockApiResponse stockApiResponse = netUtils.doPost(apiVO);
            ArrayList<NewStockVO> items = stockApiResponse.getItems(NewStockVO.class);
            if (items != null) {
                StringBuilder sb = new StringBuilder();
                for (NewStockVO item : items) {
                    String tsCode = item.getTsCode();
                    StockBasicsDTO dto = stockBasicsMapper.selectStockBasicsByTsCode(tsCode);
                    if (dto == null) {
                        sb.append(tsCode).append(",");
                    }
                }
                String sbString = sb.toString();
                if (sbString.isEmpty()) {
                    return null;
                } else {
                    Map<String, String> param = Map.of("list_status","L", "ts_code", sb.toString());
                    apiVO.setApi_name("stock_basic");
                    apiVO.setParams(param);
                    StockApiResponse response = netUtils.doPost(apiVO);
                    if (response.data()!=null) {
                        return response.getItems(StockBasicsVO.class);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    /**
     * 做退市判断和新上市股票的判断与逻辑
     * @return 成功 false， 失败 true
     */
    private boolean doDelintingAndNewStockBasics() {
        log.info("已有数据，正在进行更新操作");
        List<String> delinting = this.isDelisting();
        if (delinting != null && !delinting.isEmpty()) {
            // 处理退市股票
            log.info("正在处理退市股票");
        }

        List<StockBasicsVO> newBasicsVos = this.getNewStockBasicsData(); // 获取新上市的股票
        if (newBasicsVos != null && !newBasicsVos.isEmpty()) {
            log.info("正在处理添加新股票操作");
            for (StockBasicsVO data : newBasicsVos) {
                StockBasicsDTO dto = new StockBasicsDTO();
                BeanUtils.copyProperties(data, dto);
                stockBasicsMapper.insertStockBasics(dto);
            }

            return false;
        } else {
            return true;
        }
    }

    private void saveJson(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        log.info("数据保存成功....");
    }

    private String readJsonFile(String fileName) {

        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuilder sb = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
    }
}
