package com.xiaoxiao.stockbackend.entity.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 请求第三方股票数据的实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockApiVO {
    private String api_name;
    private String token;
    private Map<String, String> params;
    private List<String> fields;
}
