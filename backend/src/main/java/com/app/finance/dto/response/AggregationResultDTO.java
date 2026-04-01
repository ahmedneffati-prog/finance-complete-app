package com.app.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResultDTO {
    private List<String> dimensions;
    private List<String> measures;
    private List<Map<String, Object>> rows;
    private Map<String, Object> totals;
    private Integer totalRows;
}
