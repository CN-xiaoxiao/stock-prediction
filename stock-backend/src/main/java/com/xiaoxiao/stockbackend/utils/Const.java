package com.xiaoxiao.stockbackend.utils;

public class Const {
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    public static final String JWT_FREQUENCY = "jwt:frequency:";

    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public static final String VERIFY_EMAIL_DATA = "verify:email:data:";

    public static final int ORDER_CORS = -102;
    public static final int ORDER_FLOW_LIMIT = -101;

    //请求频率限制
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";

    public final static String STOCK_HOT_LIST = "stock:hot:";
}
