package com.app.finance.controller;

import com.app.finance.dto.request.ExportRequestDTO;
import com.app.finance.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @PostMapping
    public ResponseEntity<byte[]> export(@RequestBody ExportRequestDTO request) {
        byte[] data = exportService.export(request);
        String format = request.getFormat() != null ? request.getFormat().toUpperCase() : "EXCEL";
        String timestamp = LocalDateTime.now().format(FMT);

        String filename;
        MediaType mediaType;
        if ("PDF".equals(format)) {
            filename = "finance_report_" + timestamp + ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            filename = "finance_report_" + timestamp + ".xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
    }

    @GetMapping("/trades/excel")
    public ResponseEntity<byte[]> exportTradesToExcel() {
        byte[] data = exportService.exportTrades();
        String filename = "trades_" + LocalDateTime.now().format(FMT) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/market-data/excel")
    public ResponseEntity<byte[]> exportMarketDataToExcel(
            @RequestParam(required = false) String symbol) {
        byte[] data = exportService.exportMarketData(symbol);
        String filename = "market_data_" + (symbol != null ? symbol + "_" : "") + LocalDateTime.now().format(FMT) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
