package com.xiaoxiao.stockbackend.task;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.response.HotStockVO;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.Const;
import com.xiaoxiao.stockbackend.utils.net.SpiderUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class HotStockJobBean extends QuartzJobBean {
    @Resource
    SpiderUtils spiderUtils;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    StockService stockService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("正在更新热门股票榜单");
        try {
            List<HotStockVO> hotStockVOList = spiderUtils.getHotStock(1, 20);
            if (hotStockVOList != null && !hotStockVOList.isEmpty()) {
                for (int i = 1; i <= 20; i++) {
                    Boolean b = stringRedisTemplate.hasKey(Const.STOCK_HOT_LIST + i + ":");
                    if (Boolean.TRUE.equals(b)) {
                        stringRedisTemplate.delete(Const.STOCK_HOT_LIST + i + ":");
                    }
                }
                for (int i = 0; i < hotStockVOList.size(); i++) {
                    HotStockVO hotStockVO = hotStockVOList.get(i);
                    String tsCode = hotStockVO.getTsCode();
                    log.info("正在添加股票代码为[{}]的股票到热门榜单", tsCode);
                    List<StockBasicsDTO> list = stockService.getStockBasicsDTO(tsCode);

                    if (list == null || list.isEmpty()) {
                        log.warn("本地没有股票代码为[{}]的股票信息", tsCode);
                        continue;
                    }
                    StockBasicsDTO stockBasicsDTO = list.get(0);
                    if (stockBasicsDTO != null) {
                        String jsonString = JSONObject.toJSONString(stockBasicsDTO);
                        stringRedisTemplate.opsForValue().set(Const.STOCK_HOT_LIST + (i+1) + ":", jsonString);
                    }
                }
                log.info("更新热门股票榜单完成");
            } else {
                log.warn("更新热门股票失败");
            }

        } catch (IOException | InterruptedException e) {
            log.error("更新热门股票榜单发生异常: {}", e.getMessage());
        }
    }
}
