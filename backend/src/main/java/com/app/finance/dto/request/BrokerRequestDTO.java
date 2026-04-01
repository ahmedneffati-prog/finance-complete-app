package com.app.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrokerRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;
    private String country;
    private String website;
    private Boolean isActive = true;
}
