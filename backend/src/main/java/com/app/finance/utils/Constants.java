package com.app.finance.utils;

public final class Constants {
    private Constants() {}

    public static final String CACHE_STOCKS = "stocks";
    public static final String CACHE_BROKERS = "brokers";
    public static final String CACHE_MARKET_DATA = "marketData";
    public static final String CACHE_AGGREGATIONS = "aggregations";

    public static final String TRADE_TYPE_BUY = "BUY";
    public static final String TRADE_TYPE_SELL = "SELL";

    public static final String INTERVAL_1MIN = "1min";
    public static final String INTERVAL_5MIN = "5min";
    public static final String INTERVAL_15MIN = "15min";
    public static final String INTERVAL_1H = "1h";
    public static final String INTERVAL_1DAY = "1day";
    public static final String INTERVAL_1WEEK = "1week";

    public static final String AGGREGATE_SUM = "SUM";
    public static final String AGGREGATE_AVG = "AVG";
    public static final String AGGREGATE_MAX = "MAX";
    public static final String AGGREGATE_MIN = "MIN";
    public static final String AGGREGATE_COUNT = "COUNT";

    public static final String EXPORT_EXCEL = "EXCEL";
    public static final String EXPORT_PDF = "PDF";

    public static final String WS_TOPIC_LIVE_DATA = "/topic/live-data";
    public static final String WS_TOPIC_MARKET = "/topic/market/";
}
