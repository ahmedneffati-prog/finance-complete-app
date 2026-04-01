package com.app.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twelve.data")
@Data
public class TwelveDataProperties {
    private String apiKey;
    private String baseUrl;
    private String wsUrl;
}
