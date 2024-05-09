package com.xiaoxiao.stockpredict.model.predictDemo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName MaxAndMinArrayVO
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
public class MaxAndMinArrayVO implements Serializable {
    String symbol;
    double[] minArray;
    double[] maxArray;
}
