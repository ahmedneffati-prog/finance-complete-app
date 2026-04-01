package com.app.finance.controller;

import com.app.finance.dto.request.AggregationRequestDTO;
import com.app.finance.dto.request.PivotTableRequestDTO;
import com.app.finance.dto.response.AggregationResponseDTO;
import com.app.finance.dto.response.AggregationResultDTO;
import com.app.finance.dto.response.ApiResponseDTO;
import com.app.finance.service.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aggregation")
@RequiredArgsConstructor
public class AggregationController {

    private final AggregationService aggregationService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<AggregationResultDTO>> aggregate(
            @RequestBody AggregationRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(aggregationService.aggregate(request)));
    }

    @PostMapping("/pivot")
    public ResponseEntity<ApiResponseDTO<AggregationResponseDTO>> pivot(
            @RequestBody PivotTableRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(aggregationService.getPivotData(request)));
    }

    @GetMapping("/trades/summary")
    public ResponseEntity<ApiResponseDTO<AggregationResponseDTO>> getTradesSummary() {
        return ResponseEntity.ok(ApiResponseDTO.success(aggregationService.getTradesSummary()));
    }

    @GetMapping("/market/summary")
    public ResponseEntity<ApiResponseDTO<AggregationResponseDTO>> getMarketSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                aggregationService.getMarketDataSummary(startDate, endDate)));
    }
}
