package com.app.finance.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AggregationRequestDTO {
    private String entity; // trades, market_data, stocks
    private List<String> dimensions; // GROUP BY fields
    private List<MeasureDTO> measures; // aggregate functions
    private Map<String, Object> filters;
    private List<String> sortBy;
    private String sortDirection = "ASC";
    private Integer limit = 1000;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
