package com.app.finance.service;

import com.app.finance.config.TwelveDataProperties;
import com.app.finance.dto.response.MarketDataResponseDTO;
import com.app.finance.entity.MarketData;
import com.app.finance.entity.Stock;
import com.app.finance.repository.MarketDataRepository;
import com.app.finance.repository.StockRepository;
import com.app.finance.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwelveDataService {

    private final TwelveDataProperties properties;
    private final MarketDataRepository marketDataRepository;
    private final StockRepository stockRepository;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Cacheable(value = Constants.CACHE_MARKET_DATA, key = "#symbol + '_' + #interval + '_' + #outputSize")
    public List<MarketDataResponseDTO> getTimeSeries(String symbol, String interval, Integer outputSize) {
        String url = String.format("%s/time_series?symbol=%s&interval=%s&outputsize=%d&apikey=%s",
                properties.getBaseUrl(), symbol, interval,
                outputSize != null ? outputSize : 100, properties.getApiKey());

        log.info("Fetching time series for {} interval={}", symbol, interval);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("status") && "error".equals(root.get("status").asText())) {
                log.error("TwelveData API error: {}", root.get("message").asText());
                return getFromDatabase(symbol, interval);
            }

            List<MarketDataResponseDTO> results = new ArrayList<>();
            JsonNode values = root.get("values");
            if (values != null && values.isArray()) {
                Optional<Stock> stockOpt = stockRepository.findBySymbol(symbol);
                for (JsonNode node : values) {
                    MarketData md = parseMarketData(node, symbol, interval, stockOpt.orElse(null));
                    marketDataRepository.save(md);
                    results.add(toDTO(md));
                }
            }
            return results;
        } catch (Exception e) {
            log.error("Error fetching from TwelveData API: {}", e.getMessage());
            return getFromDatabase(symbol, interval);
        }
    }

    public List<MarketDataResponseDTO> getFromDatabase(String symbol, String interval) {
        return marketDataRepository.findBySymbolOrderByTimestampDesc(symbol).stream()
                .filter(md -> interval == null || interval.equals(md.getInterval()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MarketDataResponseDTO> getBySymbolAndDateRange(String symbol, LocalDateTime start, LocalDateTime end) {
        return marketDataRepository.findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Cacheable(value = Constants.CACHE_MARKET_DATA, key = "'quote_' + #symbol")
    public MarketDataResponseDTO getLatestQuote(String symbol) {
        String url = String.format("%s/quote?symbol=%s&apikey=%s",
                properties.getBaseUrl(), symbol, properties.getApiKey());
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            if (!root.has("code")) {
                return MarketDataResponseDTO.builder()
                        .symbol(symbol)
                        .close(getBD(root, "close"))
                        .open(getBD(root, "open"))
                        .high(getBD(root, "high"))
                        .low(getBD(root, "low"))
                        .volume(getBD(root, "volume"))
                        .previousClose(getBD(root, "previous_close"))
                        .changeValue(getBD(root, "change"))
                        .changePercent(getBD(root, "percent_change"))
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching quote for {}: {}", symbol, e.getMessage());
        }
        return marketDataRepository.findTopBySymbolOrderByTimestampDesc(symbol)
                .map(this::toDTO).orElse(null);
    }

    private MarketData parseMarketData(JsonNode node, String symbol, String interval, Stock stock) {
        LocalDateTime ts;
        try {
            ts = LocalDateTime.parse(node.get("datetime").asText(), FORMATTER);
        } catch (Exception e) {
            ts = LocalDateTime.now();
        }
        return MarketData.builder()
                .symbol(symbol)
                .open(getBD(node, "open"))
                .high(getBD(node, "high"))
                .low(getBD(node, "low"))
                .close(getBD(node, "close"))
                .volume(getBD(node, "volume"))
                .interval(interval)
                .timestamp(ts)
                .stock(stock)
                .build();
    }

    private BigDecimal getBD(JsonNode node, String field) {
        try {
            JsonNode n = node.get(field);
            return (n != null && !n.isNull()) ? new BigDecimal(n.asText()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public MarketDataResponseDTO toDTO(MarketData md) {
        return MarketDataResponseDTO.builder()
                .id(md.getId())
                .symbol(md.getSymbol())
                .open(md.getOpen())
                .high(md.getHigh())
                .low(md.getLow())
                .close(md.getClose())
                .adjustedClose(md.getAdjustedClose())
                .volume(md.getVolume())
                .changeValue(md.getChangeValue())
                .changePercent(md.getChangePercent())
                .previousClose(md.getPreviousClose())
                .interval(md.getInterval())
                .timestamp(md.getTimestamp())
                .build();
    }
}
