package com.app.finance.service;

import com.app.finance.dto.request.AggregationRequestDTO;
import com.app.finance.dto.request.MeasureDTO;
import com.app.finance.dto.request.PivotTableRequestDTO;
import com.app.finance.dto.response.AggregationResponseDTO;
import com.app.finance.dto.response.AggregationResultDTO;
import com.app.finance.entity.MarketData;
import com.app.finance.entity.Trade;
import com.app.finance.repository.MarketDataRepository;
import com.app.finance.repository.TradeRepository;
import com.app.finance.utils.DataAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final TradeRepository tradeRepository;
    private final MarketDataRepository marketDataRepository;
    private final DataAggregator dataAggregator;

    @Cacheable(value = "aggregations", key = "#request.toString()")
    public AggregationResultDTO aggregate(AggregationRequestDTO request) {
        List<Map<String, Object>> rawData = fetchRawData(request);
        return dataAggregator.aggregate(rawData, request);
    }

    public AggregationResponseDTO getPivotData(PivotTableRequestDTO request) {
        AggregationRequestDTO aggRequest = new AggregationRequestDTO();
        aggRequest.setEntity(request.getEntity());

        List<String> allDimensions = new ArrayList<>();
        if (request.getRows() != null) allDimensions.addAll(request.getRows());
        if (request.getColumns() != null) allDimensions.addAll(request.getColumns());
        aggRequest.setDimensions(allDimensions);
        aggRequest.setMeasures(request.getValues());
        aggRequest.setFilters(request.getFilters());

        AggregationResultDTO result = aggregate(aggRequest);

        return AggregationResponseDTO.builder()
                .data(result.getRows())
                .count(result.getTotalRows())
                .summary(result.getTotals())
                .build();
    }

    public AggregationResponseDTO getTradesSummary() {
        List<Object[]> symbolData = tradeRepository.findSymbolSummary();
        List<Map<String, Object>> rows = symbolData.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("symbol", row[0]);
            map.put("totalValue", row[1]);
            map.put("tradeCount", row[2]);
            return map;
        }).collect(Collectors.toList());

        // Totals
        BigDecimal grandTotal = rows.stream()
                .map(r -> r.get("totalValue") instanceof BigDecimal bd ? bd : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("grandTotalValue", grandTotal);
        summary.put("totalSymbols", rows.size());

        return AggregationResponseDTO.builder()
                .data(rows)
                .count(rows.size())
                .summary(summary)
                .build();
    }

    public AggregationResponseDTO getMarketDataSummary(String startDate, String endDate) {
        List<Trade> trades = tradeRepository.findAll();
        Map<String, List<Trade>> bySymbol = trades.stream().collect(Collectors.groupingBy(Trade::getSymbol));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, List<Trade>> entry : bySymbol.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("symbol", entry.getKey());
            row.put("totalTrades", entry.getValue().size());
            row.put("buyCount", entry.getValue().stream().filter(t -> "BUY".equals(t.getTradeType())).count());
            row.put("sellCount", entry.getValue().stream().filter(t -> "SELL".equals(t.getTradeType())).count());
            BigDecimal totalValue = entry.getValue().stream()
                    .map(t -> t.getTotalValue() != null ? t.getTotalValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("totalValue", totalValue.setScale(4, RoundingMode.HALF_UP));
            BigDecimal avgPrice = entry.getValue().stream()
                    .map(t -> t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(entry.getValue().size()), 4, RoundingMode.HALF_UP);
            row.put("avgPrice", avgPrice);
            rows.add(row);
        }

        return AggregationResponseDTO.builder()
                .data(rows)
                .count(rows.size())
                .summary(Map.of("totalSymbols", bySymbol.size()))
                .build();
    }

    private List<Map<String, Object>> fetchRawData(AggregationRequestDTO request) {
        String entity = request.getEntity() != null ? request.getEntity().toLowerCase() : "trades";

        return switch (entity) {
            case "trades" -> {
                List<Trade> trades = (request.getStartDate() != null && request.getEndDate() != null)
                        ? tradeRepository.findByDateRange(request.getStartDate(), request.getEndDate())
                        : tradeRepository.findAll();
                yield trades.stream().map(this::tradeToMap).collect(Collectors.toList());
            }
            case "market_data" -> {
                List<MarketData> data = marketDataRepository.findAll();
                yield data.stream().map(this::marketDataToMap).collect(Collectors.toList());
            }
            default -> Collections.emptyList();
        };
    }

    private Map<String, Object> tradeToMap(Trade t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("symbol", t.getSymbol());
        map.put("tradeType", t.getTradeType());
        map.put("quantity", t.getQuantity());
        map.put("price", t.getPrice());
        map.put("totalValue", t.getTotalValue());
        map.put("tradeDate", t.getTradeDate());
        map.put("brokerId", t.getBroker() != null ? t.getBroker().getId() : null);
        map.put("brokerName", t.getBroker() != null ? t.getBroker().getName() : null);
        map.put("stockId", t.getStock() != null ? t.getStock().getId() : null);
        map.put("sector", t.getStock() != null ? t.getStock().getSector() : null);
        return map;
    }

    private Map<String, Object> marketDataToMap(MarketData md) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", md.getId());
        map.put("symbol", md.getSymbol());
        map.put("open", md.getOpen());
        map.put("high", md.getHigh());
        map.put("low", md.getLow());
        map.put("close", md.getClose());
        map.put("volume", md.getVolume());
        map.put("changePercent", md.getChangePercent());
        map.put("interval", md.getInterval());
        map.put("timestamp", md.getTimestamp());
        return map;
    }
}
