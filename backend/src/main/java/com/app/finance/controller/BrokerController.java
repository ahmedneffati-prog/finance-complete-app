package com.app.finance.controller;

import com.app.finance.dto.request.BrokerRequestDTO;
import com.app.finance.dto.response.ApiResponseDTO;
import com.app.finance.dto.response.BrokerResponseDTO;
import com.app.finance.service.BrokerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brokers")
@RequiredArgsConstructor
public class BrokerController {

    private final BrokerService brokerService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BrokerResponseDTO>>> getAllBrokers() {
        return ResponseEntity.ok(ApiResponseDTO.success(brokerService.getAllBrokers()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<BrokerResponseDTO>>> getActiveBrokers() {
        return ResponseEntity.ok(ApiResponseDTO.success(brokerService.getActiveBrokers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BrokerResponseDTO>> getBrokerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(brokerService.getBrokerById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BrokerResponseDTO>> createBroker(@Valid @RequestBody BrokerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(brokerService.createBroker(dto), "Broker created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BrokerResponseDTO>> updateBroker(
            @PathVariable Long id, @Valid @RequestBody BrokerRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success(brokerService.updateBroker(id, dto), "Broker updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBroker(@PathVariable Long id) {
        brokerService.deleteBroker(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Broker deleted successfully"));
    }
}
