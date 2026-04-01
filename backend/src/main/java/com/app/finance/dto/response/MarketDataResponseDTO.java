package com.app.finance.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MarketDataResponseDTO {
    private Long id;
    private String symbol;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal adjustedClose;
    private BigDecimal volume;
    private BigDecimal changeValue;
    private BigDecimal changePercent;
    private BigDecimal previousClose;
    private String interval;
    private LocalDateTime timestamp;
}
