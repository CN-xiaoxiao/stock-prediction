package com.xiaoxiao.stockpredict;

import com.xiaoxiao.stockpredict.model.predictDemo.StockPricePrediction;
import com.xiaoxiao.stockpredict.model.representation.PriceCategory;
import com.xiaoxiao.stockpredict.model.representation.StockDataSetIterator;
import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Main
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(StockPricePrediction.class);
    private static int exampleLength = 22;


    public static void main(String[] args) {

        try {
//            String file = new ClassPathResource("prices-split-adjusted.csv").getFile().getAbsolutePath();
            String file = "stock-predict\\src\\main\\resources\\demo.csv";
            String symbol = "000001.SZ";
            int batchSize = 64;
            double splitRatio = 0.9;
            int epochs = 100;

            log.info("Create dataSet iterator...");
            PriceCategory category = PriceCategory.ALL;
            StockDataSetIterator iterator = new StockDataSetIterator(null, file, symbol, batchSize, exampleLength, splitRatio, category);
            log.info("Load test dataset...");
            List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();
            test = test.subList(test.size() - 5, test.size());
            log.info("Test data size is " + test.size());
            log.info("Build lstm networks...");
            File locationToSave = new File("stock-predict/src/main/resources/StockPriceLSTM_ALL.zip");
            MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
            System.out.println(net.summary());
//            double max = iterator.getMaxNum(category);
//            double min = iterator.getMinNum(category);
//            predictPriceOneAhead(net, test, max, min, category);
            INDArray max = Nd4j.create(iterator.getMaxArray());
            INDArray min = Nd4j.create(iterator.getMinArray());
            System.out.println("iterator.getMaxArray() = " + Arrays.toString(Arrays.stream(iterator.getMaxArray()).toArray()));
            System.out.println("iterator.getMinArray() = " + Arrays.toString(Arrays.stream(iterator.getMinArray()).toArray()));

            predictAllCategories(net, test, max, min);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void predictPriceOneAhead (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min, PriceCategory category) {
        double[] predicts = new double[testData.size()];
        double[] actuals = new double[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            System.out.println("testData.get(i).getKey() = " + testData.get(i).getKey());
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getDouble(exampleLength - 1) * (max - min) + min;
            actuals[i] = testData.get(i).getValue().getDouble(0);
        }

        log.info("Print out Predictions and Actual Values...");
        log.info("Predict,Actual");
        for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "," + actuals[i]);
//        log.info("Plot...");
//        PlotUtil.plot(predicts, actuals, String.valueOf(category));
    }

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
//        log.info("Plot...");
//        for (int n = 0; n < 5; n++) {
//            double[] pred = new double[predicts.length];
//            double[] actu = new double[actuals.length];
//            for (int i = 0; i < predicts.length; i++) {
//                pred[i] = predicts[i].getDouble(n);
//                actu[i] = actuals[i].getDouble(n);
//            }
//            String name;
//            switch (n) {
//                case 0: name = "Stock OPEN Price"; break;
//                case 1: name = "Stock CLOSE Price"; break;
//                case 2: name = "Stock LOW Price"; break;
//                case 3: name = "Stock HIGH Price"; break;
//                case 4: name = "Stock VOLUME Amount"; break;
//                default: throw new NoSuchElementException();
//            }
//            PlotUtil.plot(pred, actu, name);
//        }
    }

}
