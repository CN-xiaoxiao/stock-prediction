package com.xiaoxiao.stockbackend.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.utils.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方股票数据响应体实体类
 * @param request_id 请求id
 * @param code 响应码
 * @param data 返回数据
 * @param has_more 是否还有数据
 */
public record StockApiResponse(String request_id, int code, Object data, boolean has_more) {

    /**
     * 得到所有响应字段
     * @return 字段集合
     */
    public List<String> getFields() {
        return JSONObject.parseObject(JSONObject.toJSONString(data))
                .getJSONArray("fields").toJavaList(String.class);
    }

    /**
     * 得到所有的数据
     *
     * @return item集合
     */
    public <T>ArrayList<T> getItems(Class<T> clazz) {

        List<ArrayList> items = JSONObject.parseObject(JSONObject.toJSONString(data))
                .getJSONArray("items").toJavaList(ArrayList.class);
        ArrayList<T> result = new ArrayList<>();

        try {
            for (ArrayList item : items) {
                JSONArray jsonArray = (JSONArray) item;
                Object[] array = jsonArray.stream().map((v) -> {
                    if (v instanceof BigDecimal) {
                        return ((BigDecimal) v).doubleValue();
                    } else {
                        return v;
                    }
                }).toArray();

                ObjectUtils utils = new ObjectUtils();
                T t = utils.objectArrayToObject(array, clazz);
                result.add(t);
            }
        } catch (Exception e) {
            return null;
        }
        return result.isEmpty() ? null : result;
    }

}
