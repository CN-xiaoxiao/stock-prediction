package com.xiaoxiao.stockbackend.controller;

import com.github.pagehelper.PageInfo;
import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.dto.Favorite;
import com.xiaoxiao.stockbackend.entity.dto.StockBasicsDTO;
import com.xiaoxiao.stockbackend.entity.vo.request.StockDailyVO;
import com.xiaoxiao.stockbackend.entity.vo.response.*;
import com.xiaoxiao.stockbackend.service.StockDailyService;
import com.xiaoxiao.stockbackend.service.StockPredictService;
import com.xiaoxiao.stockbackend.service.StockService;
import com.xiaoxiao.stockbackend.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Resource
    StockService stockService;
    @Resource
    StockDailyService stockDailyService;
    @Resource
    StockPredictService stockPredictService;

    // 获得股票基础信息
    @GetMapping("/all-basics2")
    public RestBean<PageInfo<StockBasicsDTO>> getAllStockBasicsDataS(@RequestParam @Valid int pageNum,
                                                                     @RequestParam @Valid int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockDataS(pageNum, pageSize));
    }

    // 获得股票基础信息
    @GetMapping("/all-basics")
    public RestBean<List<StockBasicsDTO>> getAllStockBasicsData(@RequestParam @Valid int pageNum,
                                                                @RequestParam @Valid int pageSize) {
        return RestBean.success(stockService.selectAllBasicsStockData(pageNum, pageSize));
    }

    // 获得热门股票列表
    @GetMapping("/hot")
    public RestBean<List<StockBasicsVO>> getHotStockList() {
        return RestBean.success(stockService.getHotStockData());
    }

    // 获取某个股票的日线数据（4个月）
    @PostMapping("/daily")
    public RestBean<StockRealListVO> getStockDailyData(@RequestBody @Valid StockDailyVO vo) {
        LocalDate date = vo.getDate();
        LocalDate date1 = date.plusMonths(-4);
        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory(vo.getTsCode(), date1);
        StockRealListVO stockRealListVO = new StockRealListVO();
        stockRealListVO.setList(stockDailyHistory);
        return stockDailyHistory != null && !stockDailyHistory.isEmpty() ?
                RestBean.success(stockRealListVO):RestBean.failure(400, "暂无数据");
    }

    // 获取某个股票的所有日线数据
    @GetMapping("/daily-all")
    public RestBean<StockRealListVO> getAllStockDailyData(@RequestParam @Valid String tsCode) {

        List<StockRealVO> stockDailyHistory = stockDailyService.getStockDailyHistory(tsCode, LocalDate.parse("2010-01-01"));
        StockRealListVO stockRealListVO = new StockRealListVO();
        stockRealListVO.setList(stockDailyHistory);
        return stockDailyHistory != null && !stockDailyHistory.isEmpty() ?
                RestBean.success(stockRealListVO):RestBean.failure(400, "暂无数据");
    }

    /**
     * 查询收藏夹里所有股票的日线信息
     * @param id
     * @return
     */
    @GetMapping("/daily-favorite")
    public RestBean<List<StockRealListVO>> getStockDailyForFavorite(@RequestAttribute("id") int id) {
        List<StockRealListVO> list = stockDailyService.getStockDailyHistoryForFavorite(id);
        return RestBean.success(list);
    }

    @GetMapping("/query")
    public RestBean<PageInfo<StockBasicsVO>> queryStockBasics(@RequestParam @Valid int pageNum,
                                                              @RequestParam @Valid int pageSize,
                                                              @RequestParam String tsCode) {
        return RestBean.success(stockService.getStockBasicsVO(pageNum, pageSize, tsCode));
    }

    @GetMapping("/favorite")
    public RestBean<FavoriteVO> queryFavorite(@RequestAttribute("id") int id) {
        return RestBean.success(stockService.queryFavoriteByUid(id));
    }

    @GetMapping("/favoriteList")
    public RestBean<List<StockBasicsVO>> queryFavoriteList(@RequestAttribute("id") int id) {
        List<StockBasicsVO> list = stockService.getStockBasicsListForFavorite(id);
        return list == null || list.isEmpty() ? RestBean.failure(401, "请求参数有误")
                : RestBean.success(list);
    }

    @PostMapping("/favorite")
    public RestBean<Void> updateFavorite(@RequestParam @Valid int uid,
                                         @RequestParam @Valid String favoriteList,
                                         @RequestAttribute("id") int id) {
        if (uid != id) return RestBean.failure(400, "请求参数有误");

        Favorite favorite = new Favorite();
        favorite.setUid(uid);
        favorite.setFavoriteList(favoriteList);
        if (stockService.updateFavorite(favorite)) return RestBean.success();
        else return RestBean.failure(400, "更新失败");
    }

    @PutMapping("/favorite")
    public RestBean<String> addFavorite(@RequestHeader("Authorization") String token,
                                         @RequestParam @Valid String tsCode) {
        boolean flag = stockService.addFavorite(tsCode, token);
        return flag ? RestBean.success() : RestBean.failure(400, "请求失败");
    }

    @GetMapping("/favorite-delete")
    public RestBean<String> deleteFavorite(@RequestHeader("Authorization") String token,
                                           @RequestParam @Valid String tsCode) {
        boolean flag = stockService.deleteFavorite(tsCode, token);

        return flag ? RestBean.success() : RestBean.failure(401, "请求失败");
    }

    @GetMapping("/register")
    public RestBean<String> registerTreating() {
        return RestBean.success(stockPredictService.registerToken());
    }

    /**
     * 预测一条股票信息（预测明天）
     * @param tsCode ts股票代码
     * @return 预测的结果
     */
    @GetMapping("/predict")
    public RestBean<List<StockPredictVO>> doPredict(@RequestParam @Valid String tsCode) {
        List<StockPredictVO> predict = stockPredictService.predict(tsCode);
        if (predict != null && !predict.isEmpty()) return RestBean.success(predict);
        else return RestBean.failure(400, "服务器内部错误");
    }

    /**
     * 获取四个月的股票预测信息
     * @param tsCode
     * @return
     */
    @GetMapping("/predict-list")
    public RestBean<List<StockPreVO>> predictList(@RequestParam @Valid String tsCode) {
        LocalDate date = LocalDate.now();
        LocalDate startDate = date.plusMonths(-4);
        LocalDate endDate = date.plusDays(1);

        List<StockPreVO> list = stockPredictService.getPredictList(tsCode, startDate, endDate);

        return list == null || list.isEmpty() ? RestBean.failure(401, "暂无数据") : RestBean.success(list);
    }
}
