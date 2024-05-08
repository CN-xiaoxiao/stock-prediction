package com.xiaoxiao.stockpredict.model.predict;


import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockpredict.model.RecurrentNets;
import com.xiaoxiao.stockpredict.model.representation.PriceCategory;
import com.xiaoxiao.stockpredict.model.representation.StockDataSetIterator;
import com.xiaoxiao.stockpredict.model.utils.PlotUtil;
import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;


public class StockPricePrediction {

    private static final Logger log = LoggerFactory.getLogger(StockPricePrediction.class);

    private static int exampleLength = 22;

    public static void main (String[] args) throws IOException {
//        String file = new ClassPathResource("prices-split-adjusted.csv").getFile().getAbsolutePath();
//        String symbol = "GOOG"; // 股票名称
        String folderPath = "stock-predict/src/main/resources/data";
//        String file = "stock-predict\\src\\main\\resources\\demo.csv";
//        String symbol = "000001.SZ";
        List<String> stockList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                .filter(Files::isRegularFile)
                .forEach(v -> stockList.add(v.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : stockList) {
            String sy;
            String filename = folderPath.concat("/").concat(s);
            int dotIndex = s.lastIndexOf(".");
            sy = s.substring(0, dotIndex);
            doTraining(filename, sy);
            break;
        }
    }

    private static void doTraining(String file, String symbol) throws IOException {
        int batchSize = 64; // 一次训练选取的样本个数
        double splitRatio = 0.9;    // 学习率
        int epochs = 120;   // 训练次数
        log.info("正在执行股票[{}]的训练任务...", symbol);
        log.info("Create dataSet iterator...");
        PriceCategory category = PriceCategory.ALL;
        StockDataSetIterator iterator = new StockDataSetIterator(null, file, symbol, batchSize, exampleLength, splitRatio, category);
        log.info("Load test dataset...");
        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();

        log.info("Build lstm networks...");
        MultiLayerNetwork net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

        log.info("Training...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < epochs; i++) {
            while (iterator.hasNext()) net.fit(iterator.next());
            iterator.reset();
            net.rnnClearPreviousState();
        }
        long end = System.currentTimeMillis();
        log.info("共耗时 {} 分钟", (end - start)*1.0 / 1000.0 / 60.0);

        log.info("Saving model...");
        File locationToSave = new File("stock-predict/src/main/resources/model/MODEL_"
                .concat(symbol).concat(".zip"));
        if (!locationToSave.getParentFile().exists()) {
            locationToSave.getParentFile().mkdirs();
        }
        ModelSerializer.writeModel(net, locationToSave, true);
        log.info("Load model...");
        net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        log.info("Testing...");
        if (category.equals(PriceCategory.ALL)) {
            INDArray max = Nd4j.create(iterator.getMaxArray());
            INDArray min = Nd4j.create(iterator.getMinArray());
            predictAllCategories(net, test, max, min);
        } else {
            double max = iterator.getMaxNum(category);
            double min = iterator.getMinNum(category);
            predictPriceOneAhead(net, test, max, min, category);
        }
        log.info("Save model max and min arr...");
        double[] minArray = iterator.getMinArray();
        double[] maxArray = iterator.getMaxArray();
        MaxAndMinArrayVO maxAndMinArrayVO = new MaxAndMinArrayVO();
        maxAndMinArrayVO.setSymbol(symbol);
        maxAndMinArrayVO.setMinArray(minArray);
        maxAndMinArrayVO.setMaxArray(maxArray);
        String jsonString = JSONObject.toJSONString(maxAndMinArrayVO);
        File newFile = new File("stock-predict/src/main/resources/array/".concat("MAX_MIN_ARRAY_").concat(symbol).concat(".json"));
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }
        // 序列化对象并写入文件
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(newFile.toPath()), StandardCharsets.UTF_8))) {
            writer.write(jsonString);
//            log.info("[{}]的最大值和最小值数组已保存在[{}]...", symbol, newFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("股票[{}]训练完成...", symbol);
        log.info("模型保存在[{}]", locationToSave.getAbsolutePath());
        log.info("最大值和最小值数组保存在[{}]", newFile.getAbsolutePath());
    }

    /** Predict one feature of a stock one-day ahead */
    private static void predictPriceOneAhead (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min, PriceCategory category) {
        double[] predicts = new double[testData.size()];
        double[] actuals = new double[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getDouble(exampleLength - 1) * (max - min) + min;
            actuals[i] = testData.get(i).getValue().getDouble(0);
        }
        log.info("Print out Predictions and Actual Values...");
        log.info("Predict,Actual");
        for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "," + actuals[i]);
        log.info("Plot...");
        PlotUtil.plot(predicts, actuals, String.valueOf(category));
    }

    private static void predictPriceMultiple (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min) {
        // TODO
    }

    /** Predict all the features (open, close, low, high prices and volume) of a stock one-day ahead */
    private static void predictAllCategories (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, INDArray max, INDArray min) {
        INDArray[] predicts = new INDArray[testData.size()];
        INDArray[] actuals = new INDArray[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getRow(exampleLength - 1).mul(max.sub(min)).add(min);
            actuals[i] = testData.get(i).getValue();
        }
        log.info("Print out Predictions and Actual Values...");
        log.info("Predict\tActual");
        for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "\t" + actuals[i]);
        log.info("Plot...");
        for (int n = 0; n < 5; n++) {
            double[] pred = new double[predicts.length];
            double[] actu = new double[actuals.length];
            for (int i = 0; i < predicts.length; i++) {
                pred[i] = predicts[i].getDouble(n);
                actu[i] = actuals[i].getDouble(n);
            }
            String name;
            switch (n) {
                case 0: name = "Stock OPEN Price"; break;
                case 1: name = "Stock CLOSE Price"; break;
                case 2: name = "Stock LOW Price"; break;
                case 3: name = "Stock HIGH Price"; break;
                case 4: name = "Stock VOLUME Amount"; break;
                default: throw new NoSuchElementException();
            }
            PlotUtil.plot(pred, actu, name);
        }
    }

}
