package com.app.finance.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PivotTableRequestDTO {
    private String entity; // trades, market_data
    private List<String> rows;    // row dimensions
    private List<String> columns; // column dimensions
    private List<MeasureDTO> values; // measures
    private Map<String, Object> filters;
    private String startDate;
    private String endDate;
}
