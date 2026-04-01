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
public class AggregationResponseDTO {
    private List<Map<String, Object>> data;
    private Integer count;
    private Map<String, Object> summary;
}
