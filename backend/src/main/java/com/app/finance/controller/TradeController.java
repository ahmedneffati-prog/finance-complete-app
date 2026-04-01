package com.app.finance.controller;

import com.app.finance.dto.request.TradeRequestDTO;
import com.app.finance.dto.response.ApiResponseDTO;
import com.app.finance.dto.response.TradeResponseDTO;
import com.app.finance.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TradeResponseDTO>>> getAllTrades() {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.getAllTrades()));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TradeResponseDTO>> getTradesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "tradeDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = "DESC".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(tradeService.getTradesPaged(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TradeResponseDTO>> getTradeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.getTradeById(id)));
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<ApiResponseDTO<List<TradeResponseDTO>>> getBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.getTradesBySymbol(symbol)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDTO<List<TradeResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.getTradesByDateRange(start, end)));
    }

    @GetMapping("/broker/{brokerId}")
    public ResponseEntity<ApiResponseDTO<List<TradeResponseDTO>>> getByBroker(@PathVariable Long brokerId) {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.getTradesByBroker(brokerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<TradeResponseDTO>> createTrade(@Valid @RequestBody TradeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(tradeService.createTrade(dto), "Trade created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TradeResponseDTO>> updateTrade(
            @PathVariable Long id, @Valid @RequestBody TradeRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success(tradeService.updateTrade(id, dto), "Trade updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Trade deleted successfully"));
    }
}
