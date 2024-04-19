package com.xiaoxiao.stockbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockApiVO;
import com.xiaoxiao.stockbackend.entity.vo.response.NewStockVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockApiResponse;
import com.xiaoxiao.stockbackend.entity.vo.response.StockBasicsVO;
import com.xiaoxiao.stockbackend.entity.vo.response.StockRealVO;
import com.xiaoxiao.stockbackend.mapper.StockBasicsMapper;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.net.NetUtils;
import com.xiaoxiao.stockbackend.utils.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Resource
    NetUtils netUtils;
    @Resource
    StockBasicsMapper stockBasicsMapper;
    @Resource
    SnowflakeIdGenerator idGenerator;
    @Resource
    private SqlSessionFactory sqlSessionFactory;


    /**
     * 得到每天的股票交易数据
     * @param tsCode
     * @param startDate
     * @return
     */
    @Override
    public StockRealVO getDailyStockData(String tsCode, Date startDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(startDate);

        // TODO 股票会出现休市
        Map<String, String> params = Map.of("ts_code", tsCode, "start_date", date, "end_date", date);

        StockApiVO vo = createApiVO("daily", netUtils.getToken(), params, null);
        StockApiResponse response;

        try {
            response = netUtils.doPost(vo);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Objects.requireNonNull(response.getItems(StockRealVO.class)).get(0);
    }

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
        int count = stockBasicsMapper.countStockBasics();
        if (count <= 0) {
            Map<String, String> params = Map.of("list_status","L");
            StockApiVO vo = createApiVO("stock_basic", netUtils.getToken(), params, null);

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
            if (doDelistingAndNewStockBasics()) return null;
        }
        return null;
    }

    /**
     * 根据ts_code来查询股票基础信息
     * @param tsCode 股票ts代码
     * @return StockBasicsDTO 股票基础信息实体类
     */
    @Override
    public StockBasicsDTO getStockBasicsDTO(String tsCode) {
        return stockBasicsMapper.fuzzyQueryStockBasicsByTsCode(tsCode);
    }

    // 判断是否退市，第三方接口没有，只能爬虫爬取。
    private List<String> isDelisting() {
        return null;
    }

    /**
     * 查询新上市的股票
     * @return 新上市的 StockBasicsVO 股票集合
     */
    private List<StockBasicsVO> getNewStockBasicsData() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());
        Map<String, String> params = Map.of("start_date",date, "end_date", date);
        StockApiVO apiVO = createApiVO("new_share", netUtils.getToken(), params, null);

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
     * 快速创建第三方api接口的请求类
     * @param apiName api接口名称
     * @param token token
     * @param params 请求参数
     * @param fields 请求字段
     * @return 第三方api请求接口类
     */
    private StockApiVO createApiVO(String apiName, String token, Map<String, String> params, List<String> fields) {
        return new StockApiVO(apiName, token, params, fields);
    }

    /**
     * 做退市判断和新上市股票的判断与逻辑
     * @return
     */
    private boolean doDelistingAndNewStockBasics() {
        log.info("已有数据，正在进行更新操作");
        List<String> delisting = this.isDelisting();
        if (delisting != null) {
            // 处理退市股票
            log.info("正在处理退市股票");
        }

        List<StockBasicsVO> newBasicsVos = this.getNewStockBasicsData();
        if (newBasicsVos != null && !newBasicsVos.isEmpty()) {
            log.info("正在处理添加新股票操作");
            List<StockBasicsVO> newStockBasicsData = this.getNewStockBasicsData();
            if (newStockBasicsData == null || newStockBasicsData.isEmpty()) {
                return true;
            } else {
                for (StockBasicsVO data : newStockBasicsData) {
                    StockBasicsDTO dto = new StockBasicsDTO();
                    BeanUtils.copyProperties(data, dto);
                    stockBasicsMapper.insertStockBasics(dto);
                }
            }
        }
        return false;
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
