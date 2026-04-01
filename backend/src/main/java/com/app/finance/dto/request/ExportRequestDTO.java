package com.app.finance.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExportRequestDTO {
    private String format; // EXCEL, PDF
    private String entity; // trades, market_data, aggregation
    private List<String> columns;
    private Map<String, Object> filters;
    private AggregationRequestDTO aggregation;
    private String startDate;
    private String endDate;
}
