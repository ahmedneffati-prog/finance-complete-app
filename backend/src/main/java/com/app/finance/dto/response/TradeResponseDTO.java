package com.app.finance.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeResponseDTO {
    private Long id;
    private String symbol;
    private String tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private LocalDateTime tradeDate;
    private LocalDateTime createdAt;
    private Long brokerId;
    private String brokerName;
    private Long stockId;
    private String stockName;
}
