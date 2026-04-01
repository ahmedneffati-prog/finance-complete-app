package com.app.finance.service.twelve;

import com.app.finance.dto.response.LiveMarketDataDTO;
import com.app.finance.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TwelveDataMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void handleMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String event = root.has("event") ? root.get("event").asText() : "";

            switch (event) {
                case "price" -> handlePriceUpdate(root);
                case "subscribe-status" -> log.info("Subscribe status: {}", message);
                case "heartbeat" -> log.debug("Heartbeat received");
                default -> log.debug("Unknown event: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    private void handlePriceUpdate(JsonNode root) {
        try {
            String symbol = root.has("symbol") ? root.get("symbol").asText() : "";
            BigDecimal price = getDecimal(root, "price");

            LiveMarketDataDTO dto = LiveMarketDataDTO.builder()
                    .symbol(symbol)
                    .price(price)
                    .timestamp(LocalDateTime.now())
                    .exchange(root.has("exchange") ? root.get("exchange").asText() : null)
                    .currency(root.has("currency") ? root.get("currency").asText() : null)
                    .build();

            // Broadcast to general topic
            messagingTemplate.convertAndSend(Constants.WS_TOPIC_LIVE_DATA, dto);
            // Broadcast to symbol-specific topic
            messagingTemplate.convertAndSend(Constants.WS_TOPIC_MARKET + symbol, dto);

            log.debug("Price update: {} = {}", symbol, price);
        } catch (Exception e) {
            log.error("Error handling price update: {}", e.getMessage());
        }
    }

    private BigDecimal getDecimal(JsonNode node, String field) {
        try {
            return node.has(field) ? new BigDecimal(node.get(field).asText()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
