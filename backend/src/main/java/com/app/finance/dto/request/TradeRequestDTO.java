package com.app.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeRequestDTO {
    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Trade type is required")
    private String tradeType;

    @NotNull @Positive
    private BigDecimal quantity;

    @NotNull @Positive
    private BigDecimal price;

    private LocalDateTime tradeDate;
    private Long brokerId;
    private Long stockId;
}
