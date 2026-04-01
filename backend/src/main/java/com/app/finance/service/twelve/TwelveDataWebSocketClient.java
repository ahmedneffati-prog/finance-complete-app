package com.app.finance.service.twelve;

import com.app.finance.config.TwelveDataProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TwelveDataWebSocketClient {

    private final TwelveDataProperties properties;
    private final TwelveDataMessageHandler messageHandler;
    private final ObjectMapper objectMapper;

    private WebSocketClient webSocketClient;
    private final Set<String> subscribedSymbols = Collections.synchronizedSet(new HashSet<>());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        connect();
        // Reconnect check every 30 seconds
        scheduler.scheduleAtFixedRate(this::ensureConnected, 30, 30, TimeUnit.SECONDS);
    }

    private void connect() {
        try {
            URI uri = URI.create(properties.getWsUrl() + "?apikey=" + properties.getApiKey());
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Connected to TwelveData WebSocket");
                    if (!subscribedSymbols.isEmpty()) {
                        resubscribeAll();
                    }
                }

                @Override
                public void onMessage(String message) {
                    messageHandler.handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("TwelveData WebSocket closed: {} - {}", code, reason);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("TwelveData WebSocket error: {}", ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            log.error("Failed to connect to TwelveData WebSocket: {}", e.getMessage());
        }
    }

    private void ensureConnected() {
        if (webSocketClient == null || webSocketClient.isClosed()) {
            log.info("Reconnecting to TwelveData WebSocket...");
            connect();
        }
    }

    public void subscribe(List<String> symbols) {
        subscribedSymbols.addAll(symbols);
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("action", "subscribe");
                payload.put("params", Map.of("symbols", String.join(",", symbols)));
                webSocketClient.send(objectMapper.writeValueAsString(payload));
                log.info("Subscribed to symbols: {}", symbols);
            } catch (Exception e) {
                log.error("Error subscribing: {}", e.getMessage());
            }
        }
    }

    public void unsubscribe(List<String> symbols) {
        subscribedSymbols.removeAll(symbols);
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("action", "unsubscribe");
                payload.put("params", Map.of("symbols", String.join(",", symbols)));
                webSocketClient.send(objectMapper.writeValueAsString(payload));
                log.info("Unsubscribed from symbols: {}", symbols);
            } catch (Exception e) {
                log.error("Error unsubscribing: {}", e.getMessage());
            }
        }
    }

    private void resubscribeAll() {
        if (!subscribedSymbols.isEmpty()) {
            subscribe(new ArrayList<>(subscribedSymbols));
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public Set<String> getSubscribedSymbols() {
        return Collections.unmodifiableSet(subscribedSymbols);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        if (webSocketClient != null && !webSocketClient.isClosed()) {
            webSocketClient.close();
        }
    }
}
