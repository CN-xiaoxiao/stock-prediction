package com.xiaoxiao.stockpredict.model.representation;

import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVReader;
import com.xiaoxiao.stockpredict.entity.StockData;
import javafx.util.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StockDataSetIterator implements DataSetIterator {

    /** 类别及其索引 */
    private final Map<PriceCategory, Integer> featureMapIndex = ImmutableMap.of(PriceCategory.OPEN, 0, PriceCategory.CLOSE, 1,
            PriceCategory.LOW, 2, PriceCategory.HIGH, 3, PriceCategory.VOLUME, 4);

    private final int VECTOR_SIZE = 5; // 股票数据的特征数
    private int miniBatchSize; // mini-batch size
    private int exampleLength = 22; // default 22, say, 22 working days per month 默认为22，例如每月22个工作日
    private int predictLength = 1; // default 1, say, one day ahead prediction 默认值1，比如提前一天预测

    /** 股票数据集中每个特征的最小值 */
    private double[] minArray = new double[VECTOR_SIZE];
    /** 股票数据集中各特征的最大值 */
    private double[] maxArray = new double[VECTOR_SIZE];

    /** feature to be selected as a training target */
    private PriceCategory category; // 要选择的特征作为训练目标

    /** mini-batch offset */
    private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

    /** 用于训练的股票数据集 */
    private List<StockData> train;
    /** adjusted stock dataset for testing */
    private List<Pair<INDArray, INDArray>> test; // 调整股票数据集进行测试

    private List<String> dateList;

    public List<String> getDateList() {
        return this.dateList;
    }

    public StockDataSetIterator (List<StockData> dataList,
                                 String filename,
                                 String symbol,
                                 int miniBatchSize,
                                 int exampleLength,
                                 double splitRatio,
                                 PriceCategory category) {
        List<StockData> stockDataList = dataList;
        if (dataList == null || dataList.isEmpty()) {
            stockDataList = readStockDataFromFile(filename, symbol);
        } else {
            initMaxAndMinArr(stockDataList);
        }
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        this.category = category;
        int split = (int) Math.round(stockDataList.size() * splitRatio);
        train = stockDataList.subList(0, split);
        test = generateTestDataSet(stockDataList.subList(split, stockDataList.size()));
        initializeOffsets();
    }

    private void initMaxAndMinArr(List<StockData> stockDataList) {
        double maxOpen = stockDataList.stream().map(StockData::getOpen).mapToDouble(s -> s).max().orElse(0);
        double minOpen = stockDataList.stream().map(StockData::getOpen).mapToDouble(s -> s).min().orElse(0);
        double maxClose = stockDataList.stream().map(StockData::getClose).mapToDouble(s -> s).max().orElse(0);
        double minClose = stockDataList.stream().map(StockData::getClose).mapToDouble(s -> s).min().orElse(0);
        double maxLow = stockDataList.stream().map(StockData::getLow).mapToDouble(s -> s).max().orElse(0);
        double minLow = stockDataList.stream().map(StockData::getLow).mapToDouble(s -> s).min().orElse(0);
        double maxHigh = stockDataList.stream().map(StockData::getHigh).mapToDouble(s -> s).max().orElse(0);
        double minHigh = stockDataList.stream().map(StockData::getHigh).mapToDouble(s -> s).min().orElse(0);
        double maxVolume = stockDataList.stream().map(StockData::getVolume).mapToDouble(s -> s).max().orElse(0);
        double minVolume = stockDataList.stream().map(StockData::getVolume).mapToDouble(s -> s).min().orElse(0);
        minArray = new double[]{minOpen, minClose, minLow, minHigh, minVolume};
        maxArray = new double[]{maxOpen, maxClose, maxLow, maxHigh, maxVolume};
    }

    /** initialize the mini-batch offsets */
    private void initializeOffsets () {
        exampleStartOffsets.clear();
        int window = exampleLength + predictLength;
        for (int i = 0; i < train.size() - window; i++) {
            exampleStartOffsets.add(i);
        }
    }

    public List<Pair<INDArray, INDArray>> getTestDataSet() {
        return test;
    }

    public double[] getMaxArray() {
        return maxArray;
    }

    public double[] getMinArray() {
        return minArray;
    }

    public double getMaxNum (PriceCategory category) {
        return maxArray[featureMapIndex.get(category)];
    }

    public double getMinNum (PriceCategory category) {
        return minArray[featureMapIndex.get(category)];
    }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();
        int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
        INDArray input = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        INDArray label;
        if (category.equals(PriceCategory.ALL)) {
            label = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        } else {
            label = Nd4j.create(new int[] {actualMiniBatchSize, predictLength, exampleLength}, 'f');
        }

        for (int index = 0; index < actualMiniBatchSize; index++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
            StockData curData = train.get(startIdx);
            StockData nextData;
            for (int i = startIdx; i < endIdx; i++) {
                int c = i - startIdx;
                input.putScalar(new int[] {index, 0, c}, (curData.getOpen() - minArray[0]) / (maxArray[0] - minArray[0]));
                input.putScalar(new int[] {index, 1, c}, (curData.getClose() - minArray[1]) / (maxArray[1] - minArray[1]));
                input.putScalar(new int[] {index, 2, c}, (curData.getLow() - minArray[2]) / (maxArray[2] - minArray[2]));
                input.putScalar(new int[] {index, 3, c}, (curData.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]));
                input.putScalar(new int[] {index, 4, c}, (curData.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]));
                nextData = train.get(i + 1);
                if (category.equals(PriceCategory.ALL)) {
                    label.putScalar(new int[] {index, 0, c}, (nextData.getOpen() - minArray[1]) / (maxArray[1] - minArray[1]));
                    label.putScalar(new int[] {index, 1, c}, (nextData.getClose() - minArray[1]) / (maxArray[1] - minArray[1]));
                    label.putScalar(new int[] {index, 2, c}, (nextData.getLow() - minArray[2]) / (maxArray[2] - minArray[2]));
                    label.putScalar(new int[] {index, 3, c}, (nextData.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]));
                    label.putScalar(new int[] {index, 4, c}, (nextData.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]));
                } else {
                    label.putScalar(new int[]{index, 0, c}, feedLabel(nextData));
                }
                curData = nextData;
            }
            if (exampleStartOffsets.isEmpty()) break;
        }
        return new DataSet(input, label);
    }

    private double feedLabel(StockData data) {
        double value;
        switch (category) {
            case OPEN: value = (data.getOpen() - minArray[0]) / (maxArray[0] - minArray[0]); break;
            case CLOSE: value = (data.getClose() - minArray[1]) / (maxArray[1] - minArray[1]); break;
            case LOW: value = (data.getLow() - minArray[2]) / (maxArray[2] - minArray[2]); break;
            case HIGH: value = (data.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]); break;
            case VOLUME: value = (data.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]); break;
            default: throw new NoSuchElementException();
        }
        return value;
    }

    @Override
    public int totalExamples() {
        return train.size() - exampleLength - predictLength;
    }

    @Override
    public int inputColumns() {
        return VECTOR_SIZE;
    }

    @Override
    public int totalOutcomes() {
        if (this.category.equals(PriceCategory.ALL)) return VECTOR_SIZE;
        else return predictLength;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        initializeOffsets();
    }

    @Override
    public int batch() {
        return miniBatchSize;
    }

    @Override
    public int cursor() {
        return totalExamples() - exampleStartOffsets.size();
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override public List<String> getLabels() {
        throw new UnsupportedOperationException("Not Implemented"); }


    @Override public boolean hasNext() {
        return !exampleStartOffsets.isEmpty();
    }

    @Override public DataSet next() {
        return next(miniBatchSize);
    }

    private List<Pair<INDArray, INDArray>> generateTestDataSet (List<StockData> stockDataList) {
    	int window = exampleLength + predictLength;
    	List<Pair<INDArray, INDArray>> test = new ArrayList<>();
        List<String> dateListTemp = new ArrayList<>();
    	for (int i = 0; i < stockDataList.size() - window; i++) {
    		INDArray input = Nd4j.create(new int[] {exampleLength, VECTOR_SIZE}, 'f');
    		for (int j = i; j < i + exampleLength; j++) {
    			StockData stock = stockDataList.get(j);
    			input.putScalar(new int[] {j - i, 0}, (stock.getOpen() - minArray[0]) / (maxArray[0] - minArray[0]));
    			input.putScalar(new int[] {j - i, 1}, (stock.getClose() - minArray[1]) / (maxArray[1] - minArray[1]));
    			input.putScalar(new int[] {j - i, 2}, (stock.getLow() - minArray[2]) / (maxArray[2] - minArray[2]));
    			input.putScalar(new int[] {j - i, 3}, (stock.getHigh() - minArray[3]) / (maxArray[3] - minArray[3]));
    			input.putScalar(new int[] {j - i, 4}, (stock.getVolume() - minArray[4]) / (maxArray[4] - minArray[4]));
    		}
            StockData stock = stockDataList.get(i + exampleLength);
            INDArray label;
            if (category.equals(PriceCategory.ALL)) {
                label = Nd4j.create(new int[]{VECTOR_SIZE}, 'f'); // ordering is set as 'f', faster construct
                label.putScalar(new int[] {0}, stock.getOpen());
                label.putScalar(new int[] {1}, stock.getClose());
                label.putScalar(new int[] {2}, stock.getLow());
                label.putScalar(new int[] {3}, stock.getHigh());
                label.putScalar(new int[] {4}, stock.getVolume());
            } else {
                label = Nd4j.create(new int[] {1}, 'f');
                switch (category) {
                    case OPEN: label.putScalar(new int[] {0}, stock.getOpen()); break;
                    case CLOSE: label.putScalar(new int[] {0}, stock.getClose()); break;
                    case LOW: label.putScalar(new int[] {0}, stock.getLow()); break;
                    case HIGH: label.putScalar(new int[] {0}, stock.getHigh()); break;
                    case VOLUME: label.putScalar(new int[] {0}, stock.getVolume()); break;
                    default: throw new NoSuchElementException();
                }
            }
    		test.add(new Pair<>(input, label));
            dateListTemp.add(stock.getDate());
    	}
        this.dateList = dateListTemp;
    	return test;
    }

    /**
     * 加载数据集，并找到数据集中最小和最大的数据
     * @param filename 文件名
     * @param symbol 股票代码
     * @return 股票交易数据集
     */
	private List<StockData> readStockDataFromFile (String filename, String symbol) {
        List<StockData> stockDataList = new ArrayList<>();
        try {
            for (int i = 0; i < maxArray.length; i++) { // 初始化Max和min数组
                maxArray[i] = Double.MIN_VALUE;
                minArray[i] = Double.MAX_VALUE;
            }
            List<String[]> list = new CSVReader(new FileReader(filename)).readAll(); // 加载列表中的所有元素
            for (String[] arr : list) {
                if (!arr[1].equals(symbol)) continue;
                double[] nums = new double[VECTOR_SIZE];
                for (int i = 0; i < arr.length - 2; i++) {
                    nums[i] = Double.valueOf(arr[i + 2]);
                    if (nums[i] > maxArray[i]) maxArray[i] = nums[i];
                    if (nums[i] < minArray[i]) minArray[i] = nums[i];
                }
                stockDataList.add(new StockData(arr[0], arr[1], nums[0], nums[1], nums[2], nums[3], nums[4]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockDataList;
    }
}
