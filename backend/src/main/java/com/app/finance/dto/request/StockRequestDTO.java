package com.app.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockRequestDTO {
    @NotBlank(message = "Symbol is required")
    private String symbol;
    @NotBlank(message = "Name is required")
    private String name;
    private String sector;
    private String exchange;
    private String currency;
    private BigDecimal marketCap;
    private Boolean isActive = true;
}
