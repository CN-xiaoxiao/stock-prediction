package com.xiaoxiao.stockbackend.entity.vo.response;

import com.alibaba.fastjson2.JSONObject;

public record DataTreatingResponse(int code, String message, Object data) {
    public boolean success() {
        return code == 200;
    }

    public JSONObject asJson() {
        return JSONObject.from(data);
    }

    public String asString() {
        return data.toString();
    }

    public static DataTreatingResponse errorResponse(Exception e) {
        return new DataTreatingResponse(500, null, e.getMessage());
    }
}
