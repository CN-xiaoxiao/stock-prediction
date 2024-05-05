package com.xiaoxiao.stockpredict.model.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.entity.Const;
import com.xiaoxiao.stockpredict.entity.StockData;
import com.xiaoxiao.stockpredict.entity.dto.StockHistoryPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockPredictPrice;
import com.xiaoxiao.stockpredict.entity.dto.StockTestPrice;
import com.xiaoxiao.stockpredict.exception.BusinessException;
import com.xiaoxiao.stockpredict.model.RecurrentNets;
import com.xiaoxiao.stockpredict.model.representation.PriceCategory;
import com.xiaoxiao.stockpredict.model.representation.StockDataSetIterator;
import com.xiaoxiao.stockpredict.model.service.IModelService;
import javafx.util.Pair;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StockPredictWithLSTM
 * @Description 使用 LSTM 进行股票预测
 * @Author xiaoxiao
 * @Version 1.0
 */
@Slf4j
@Component
public class StockPredictWithLSTM implements IModelService {

    private static final int exampleLength = 22;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Override
    public List<StockTestPrice> modelTrain(List<StockHistoryPrice> stockHistoryPriceList, String stockCode) {
        ArrayList<StockData> dataList = new ArrayList<>();
        stockHistoryPriceList.forEach(v-> {
            StockData stockData = new StockData();
            BeanUtils.copyProperties(v, stockData);
            dataList.add(stockData);
        });
        return train(dataList, stockCode);
    }

    @Override
    public List<StockPredictPrice> modelPredict(List<StockHistoryPrice> stockHistoryPriceList, boolean flag) {
        int size = stockHistoryPriceList.size();
        if (size > 0) {
            log.info("第一组数据:{}", stockHistoryPriceList.get(0));
            log.info("最后一组数据:{}", stockHistoryPriceList.get(size-1));

            List<StockPredictPrice> stockPredictPriceList;
            if (flag) {
                stockPredictPriceList = predictMore(stockHistoryPriceList.get(0).getSymbol(), stockHistoryPriceList);
            } else {
                stockPredictPriceList = predictOneHead(stockHistoryPriceList.get(0).getSymbol(), stockHistoryPriceList);
            }
            stockPredictPriceList.forEach(v->log.info("当前股票预测价格为: {}", v));
            return stockPredictPriceList;
        }
        return new ArrayList<>();
    }

    private List<StockTestPrice> train(List<StockData> dataList, String stockCode) throws IOException {
        int batchSize = 64; // 一次训练选取的样本个数
        double splitRatio = 0.9;    // 学习率
        int epochs = 10;   // 训练次数 TODO 修改为100
        String file = "";

        log.info("Create dataSet iterator...");
        PriceCategory category = PriceCategory.ALL;
        StockDataSetIterator iterator =
                new StockDataSetIterator(dataList, file, stockCode, batchSize, exampleLength, splitRatio, category);
        log.info("Load test dataset...");
        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();

        log.info("Build lstm networks...");
        MultiLayerNetwork net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());
        net.summary();

        log.info("Training...");
        for (int i = 0; i < epochs; i++) {
            while (iterator.hasNext()) net.fit(iterator.next());
            iterator.reset();
            net.rnnClearPreviousState();
        }

        log.info("Saving model...");
        File locationToSave = new File("model/MODEL_".concat(stockCode)
                .concat(".zip"));
        File parentFile = locationToSave.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new RuntimeException("文件夹创建失败!");
        }
        ModelSerializer.writeModel(net, locationToSave, true);

        log.info("Testing...");
        INDArray max = Nd4j.create(iterator.getMaxArray());
        INDArray min = Nd4j.create(iterator.getMinArray());

        stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MIN + stockCode,
                JSONObject.toJSONString(iterator.getMinArray()));
        stringRedisTemplate.opsForValue().set(Const.MIN_MAX_ARRAY_MAX + stockCode,
                JSONObject.toJSONString(iterator.getMaxArray()));

        return predictAllCategories(net, test, iterator.getDateList(), stockCode, max, min);
    }

    @SneakyThrows
    private List<StockPredictPrice> predictOneHead(String stockCode, List<StockHistoryPrice> historyPrices) {
        if (historyPrices.size() != exampleLength) throw new BusinessException("价格数据错误！");

        MultiLayerNetwork net = lodeNetModel(stockCode);

        double[] minArray = JSONObject.parseObject(stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MIN + stockCode), double[].class);
        double[] maxArray = JSONObject.parseObject(stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MAX + stockCode), double[].class);
        if (minArray == null || maxArray == null) throw new BusinessException("最大最小值丢失，无法进行归一化！");

        log.info("Create predict data...");
        int featureVector = 5;
        double[] input = new double[featureVector * exampleLength];

        for (int i = 0; i < historyPrices.size(); i += featureVector) {
            StockHistoryPrice stockHistoryPrice = historyPrices.get(i);
            input[i] = (stockHistoryPrice.getOpen() - minArray[0]) / (maxArray[0] - minArray[0]);
            input[i+1] = (stockHistoryPrice.getClose() - minArray[1]) / (maxArray[1] - minArray[1]);
            input[i+2] = (stockHistoryPrice.getLow() - minArray[2]) / (maxArray[2] - minArray[2]);
            input[i+3] = (stockHistoryPrice.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]);
            input[i+4] = (stockHistoryPrice.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]);
        }

        INDArray inputArray = Nd4j.create(input, new int[]{22,5});
        INDArray output = net.rnnTimeStep(inputArray);

        double open = output.getDouble(0,0);
        double close = output.getDouble(0,1);
        double low = output.getDouble(0,2);
        double high = output.getDouble(0,3);
        double volume = output.getDouble(0,4);

        StockPredictPrice stockPredictPrice = new StockPredictPrice();
        stockPredictPrice.setOpen(open * (maxArray[0] - minArray[0]) + minArray[0]);
        stockPredictPrice.setClose(close * (maxArray[1] - minArray[1]) + minArray[1]);
        stockPredictPrice.setLow(low * (maxArray[2] - minArray[2]) + minArray[2]);
        stockPredictPrice.setHigh(high * (maxArray[3] - minArray[3]) + minArray[3]);
        stockPredictPrice.setVolume(volume * (maxArray[4] - minArray[4]) + minArray[4]);
        stockPredictPrice.setSymbol(stockCode);

        List<StockPredictPrice> predictPrices = new ArrayList<>();
        predictPrices.add(stockPredictPrice);

        return predictPrices;
    }

    /**
     * 预测多个股票价格（将传过来的所有历史数据都进行预测）
     * @param stockCode 股票代码
     * @param historyPrices 股票历史数据集合
     * @return 股票预测数据集合
     */
    @SneakyThrows
    private List<StockPredictPrice> predictMore(String stockCode, List<StockHistoryPrice> historyPrices) {
        if (historyPrices.isEmpty() || historyPrices.size() < 22) {
            throw new BusinessException("股票价格数据错误！");
        }

        MultiLayerNetwork net = lodeNetModel(stockCode);

        double[] minArray = JSONObject.parseObject(stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MIN + stockCode), double[].class);
        double[] maxArray = JSONObject.parseObject(stringRedisTemplate.opsForValue()
                .get(Const.MIN_MAX_ARRAY_MAX + stockCode), double[].class);
        if (minArray == null || maxArray == null) throw new BusinessException("最大最小值丢失，无法进行归一化！");

        INDArray max = Nd4j.create(maxArray);
        INDArray min = Nd4j.create(minArray);
        int featureVector = 5;

        log.info("Create predict data...");
        List<StockData> predictData = new ArrayList<>();
        historyPrices.forEach(v -> {
            StockData sd = new StockData();
            BeanUtils.copyProperties(v, sd);
            predictData.add(sd);
        });

        List<String> dateList = generateStockDateList(predictData);
        List<INDArray> indArrays = generateTestDataSet(predictData, minArray, maxArray, featureVector);
        List<Pair<INDArray, INDArray>> pairs = new ArrayList<>();
        for (INDArray indArray : indArrays) {
            pairs.add(new Pair<>(indArray, null));
        }
        List<StockTestPrice> stockTestPriceList = predictAllCategories(net, pairs, dateList, stockCode, max, min);

        List<StockPredictPrice> stockPredictPriceList = new ArrayList<>();
        stockTestPriceList.forEach(v->{
            StockPredictPrice stockPredictPrice = new StockPredictPrice();
            BeanUtils.copyProperties(v, stockPredictPrice);
            stockPredictPriceList.add(stockPredictPrice);
        });

        return stockPredictPriceList;
    }

    private List<String> generateStockDateList(List<StockData> predictData) {
        List<String> dateList = new ArrayList<>();
        predictData.forEach(v -> dateList.add(v.getDate()));
        return dateList;
    }

    private static MultiLayerNetwork lodeNetModel(String stockCode) throws IOException {
        File locationToSave = new File("model/MODEL_".concat(stockCode)
                .concat(".zip"));
        File parentFile = locationToSave.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new BusinessException("文件夹创建失败！");
        }

        log.info("Load model...");
        return ModelSerializer.restoreMultiLayerNetwork(locationToSave);
    }

    /** Predict all the features (open, close, low, high prices and volume) of a stock one-day ahead */
    private static List<StockTestPrice> predictAllCategories (MultiLayerNetwork net,
                                                              List<Pair<INDArray, INDArray>> testData,
                                                              List<String> dateList,
                                                              String stockCode,
                                                              INDArray max, INDArray min) {
        INDArray[] predicts = new INDArray[testData.size()];
        List<StockTestPrice> stockTestPriceList = new ArrayList<>();

        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getRow(exampleLength - 1).mul(max.sub(min)).add(min);

            StockTestPrice stockTestPrice = new StockTestPrice();

            String s = dateList.get(i);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(s, dtf);   // TODO 判断是否是休市日
            date = date.plusDays(1);
            stockTestPrice.setDate(dtf.format(date));

            stockTestPrice.setSymbol(stockCode);
            stockTestPrice.setOpen(predicts[i].getDouble(0));
            stockTestPrice.setClose(predicts[i].getDouble(1));
            stockTestPrice.setLow(predicts[i].getDouble(2));
            stockTestPrice.setHigh(predicts[i].getDouble(3));
            stockTestPrice.setVolume(predicts[i].getDouble(4));
            stockTestPriceList.add(stockTestPrice);
        }
        return stockTestPriceList;
    }


    private List<INDArray> generateTestDataSet (List<StockData> stockDataList,
                                                double[] minArray,
                                                double[] maxArray,
                                                int featureVector) {
        List<INDArray> test = new ArrayList<>();
        for (int i = 0; i <= stockDataList.size() - exampleLength; i++) {
            INDArray input = Nd4j.create(new int[] {exampleLength, featureVector}, 'f');
            for (int j = i; j < i + exampleLength; j++) {
                StockData stock = stockDataList.get(j);
                input.putScalar(new int[] {j - i, 0}, (stock.getOpen() - minArray[0]) / (maxArray[0] - minArray[0]));
                input.putScalar(new int[] {j - i, 1}, (stock.getClose() - minArray[1]) / (maxArray[1] - minArray[1]));
                input.putScalar(new int[] {j - i, 2}, (stock.getLow() - minArray[2]) / (maxArray[2] - minArray[2]));
                input.putScalar(new int[] {j - i, 3}, (stock.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]));
                input.putScalar(new int[] {j - i, 4}, (stock.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]));
            }
            test.add(input);
        }
        return test;
    }
}
