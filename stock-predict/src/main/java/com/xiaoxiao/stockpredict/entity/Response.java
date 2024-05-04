package com.xiaoxiao.stockpredict.entity;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName Response
 * @Description TODO
 * @Author xiaoxiao
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private int code;
    private String message;
    private Object data;

    public boolean success() {
        return code == 200;
    }

    public JSONObject asJson() {
        return JSONObject.from(data);
    }

    public String asString() {
        return data.toString();
    }

    public static Response errorResponse(Exception e) {
        return new Response(500, null, e.getMessage());
    }

    public static Response errorResponse(String message) {
        return new Response(500, null, message);
    }

    public static Response successResponse(Object data) {
        return new Response(200, "success", data);
    }

    public static Response successResponse() {
        return successResponse(null);
    }
}
