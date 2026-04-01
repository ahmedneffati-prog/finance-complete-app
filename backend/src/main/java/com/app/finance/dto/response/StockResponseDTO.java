package com.app.finance.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockResponseDTO {
    private Long id;
    private String symbol;
    private String name;
    private String sector;
    private String exchange;
    private String currency;
    private BigDecimal marketCap;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
