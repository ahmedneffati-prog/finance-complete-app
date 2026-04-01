package com.app.finance.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarketDataQueryDTO {
    private String symbol;
    private String interval;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer outputSize = 100;
}
