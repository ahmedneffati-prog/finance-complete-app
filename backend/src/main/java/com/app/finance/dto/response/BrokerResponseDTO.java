package com.app.finance.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BrokerResponseDTO {
    private Long id;
    private String name;
    private String country;
    private String website;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
