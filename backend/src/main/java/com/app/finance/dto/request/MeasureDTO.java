package com.app.finance.dto.request;

import lombok.Data;

@Data
public class MeasureDTO {
    private String field;
    private String function; // SUM, AVG, MAX, MIN, COUNT
    private String alias;
}
