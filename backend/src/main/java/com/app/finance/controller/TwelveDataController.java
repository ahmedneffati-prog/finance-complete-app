package com.app.finance.controller;

import com.app.finance.dto.response.ApiResponseDTO;
import com.app.finance.dto.response.MarketDataResponseDTO;
import com.app.finance.service.TwelveDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class TwelveDataController {

    private final TwelveDataService twelveDataService;

    @GetMapping("/time-series")
    public ResponseEntity<ApiResponseDTO<List<MarketDataResponseDTO>>> getTimeSeries(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1day") String interval,
            @RequestParam(defaultValue = "100") Integer outputSize) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                twelveDataService.getTimeSeries(symbol, interval, outputSize)));
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<ApiResponseDTO<MarketDataResponseDTO>> getLatestQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponseDTO.success(twelveDataService.getLatestQuote(symbol)));
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<ApiResponseDTO<List<MarketDataResponseDTO>>> getHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                twelveDataService.getBySymbolAndDateRange(symbol, start, end)));
    }

    @GetMapping("/db/{symbol}")
    public ResponseEntity<ApiResponseDTO<List<MarketDataResponseDTO>>> getFromDb(
            @PathVariable String symbol,
            @RequestParam(required = false) String interval) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                twelveDataService.getFromDatabase(symbol, interval)));
    }
}
