package com.app.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SubscribeRequestDTO {
    private List<String> symbols;
    private String action; // subscribe, unsubscribe
}
