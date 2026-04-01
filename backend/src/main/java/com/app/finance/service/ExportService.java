package com.app.finance.service;

import com.app.finance.dto.request.ExportRequestDTO;
import com.app.finance.entity.MarketData;
import com.app.finance.entity.Trade;
import com.app.finance.repository.MarketDataRepository;
import com.app.finance.repository.TradeRepository;
import com.app.finance.utils.ExcelExporter;
import com.app.finance.utils.PDFExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final TradeRepository tradeRepository;
    private final MarketDataRepository marketDataRepository;
    private final ExcelExporter excelExporter;
    private final PDFExporter pdfExporter;

    public byte[] export(ExportRequestDTO request) {
        String entity = request.getEntity() != null ? request.getEntity().toLowerCase() : "trades";
        String format = request.getFormat() != null ? request.getFormat().toUpperCase() : "EXCEL";

        List<Map<String, Object>> data = fetchData(entity);
        List<String> columns = getColumns(request, entity, data);
        String title = "Finance Report - " + entity.toUpperCase();

        return switch (format) {
            case "PDF" -> pdfExporter.export(title, columns, data);
            default -> excelExporter.export(entity, columns, data);
        };
    }

    public byte[] exportTrades() {
        List<Trade> trades = tradeRepository.findAll();
        List<String> headers = List.of("id", "symbol", "tradeType", "quantity", "price", "totalValue", "tradeDate");
        List<Map<String, Object>> data = trades.stream().map(t -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", t.getId());
            row.put("symbol", t.getSymbol());
            row.put("tradeType", t.getTradeType());
            row.put("quantity", t.getQuantity());
            row.put("price", t.getPrice());
            row.put("totalValue", t.getTotalValue());
            row.put("tradeDate", t.getTradeDate());
            return row;
        }).collect(Collectors.toList());
        return excelExporter.export("Trades", headers, data);
    }

    public byte[] exportMarketData(String symbol) {
        List<MarketData> marketDataList = symbol != null
                ? marketDataRepository.findBySymbolOrderByTimestampDesc(symbol)
                : marketDataRepository.findAll();
        List<String> headers = List.of("id", "symbol", "open", "high", "low", "close", "volume", "interval", "timestamp");
        List<Map<String, Object>> data = marketDataList.stream().map(md -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", md.getId());
            row.put("symbol", md.getSymbol());
            row.put("open", md.getOpen());
            row.put("high", md.getHigh());
            row.put("low", md.getLow());
            row.put("close", md.getClose());
            row.put("volume", md.getVolume());
            row.put("interval", md.getInterval());
            row.put("timestamp", md.getTimestamp());
            return row;
        }).collect(Collectors.toList());
        return excelExporter.export("MarketData_" + (symbol != null ? symbol : "all"), headers, data);
    }

    private List<Map<String, Object>> fetchData(String entity) {
        return switch (entity) {
            case "market_data" -> {
                List<MarketData> list = marketDataRepository.findAll();
                yield list.stream().map(md -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", md.getId());
                    row.put("symbol", md.getSymbol());
                    row.put("open", md.getOpen());
                    row.put("high", md.getHigh());
                    row.put("low", md.getLow());
                    row.put("close", md.getClose());
                    row.put("volume", md.getVolume());
                    row.put("interval", md.getInterval());
                    row.put("timestamp", md.getTimestamp());
                    return row;
                }).collect(Collectors.toList());
            }
            default -> {
                List<Trade> list = tradeRepository.findAll();
                yield list.stream().map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", t.getId());
                    row.put("symbol", t.getSymbol());
                    row.put("tradeType", t.getTradeType());
                    row.put("quantity", t.getQuantity());
                    row.put("price", t.getPrice());
                    row.put("totalValue", t.getTotalValue());
                    row.put("tradeDate", t.getTradeDate());
                    return row;
                }).collect(Collectors.toList());
            }
        };
    }

    private List<String> getColumns(ExportRequestDTO request, String entity, List<Map<String, Object>> data) {
        if (request.getColumns() != null && !request.getColumns().isEmpty()) {
            return request.getColumns();
        }
        if (!data.isEmpty()) {
            return new ArrayList<>(data.get(0).keySet());
        }
        return Collections.emptyList();
    }
}
