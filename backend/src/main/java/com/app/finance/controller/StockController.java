package com.app.finance.controller;

import com.app.finance.dto.request.StockRequestDTO;
import com.app.finance.dto.response.ApiResponseDTO;
import com.app.finance.dto.response.StockResponseDTO;
import com.app.finance.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<StockResponseDTO>>> getAllStocks() {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getAllStocks()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<StockResponseDTO>>> getActiveStocks() {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getActiveStocks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<StockResponseDTO>> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getStockById(id)));
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<ApiResponseDTO<StockResponseDTO>> getBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getStockBySymbol(symbol)));
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<ApiResponseDTO<List<StockResponseDTO>>> getBySector(@PathVariable String sector) {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getStocksBySector(sector)));
    }

    @GetMapping("/sectors")
    public ResponseEntity<ApiResponseDTO<List<String>>> getSectors() {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getAllSectors()));
    }

    @GetMapping("/exchanges")
    public ResponseEntity<ApiResponseDTO<List<String>>> getExchanges() {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.getAllExchanges()));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<StockResponseDTO>> createStock(@Valid @RequestBody StockRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(stockService.createStock(dto), "Stock created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<StockResponseDTO>> updateStock(
            @PathVariable Long id, @Valid @RequestBody StockRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success(stockService.updateStock(id, dto), "Stock updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Stock deleted successfully"));
    }
}
