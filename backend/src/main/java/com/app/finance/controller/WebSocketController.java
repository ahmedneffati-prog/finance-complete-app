package com.app.finance.controller;

import com.app.finance.dto.request.SubscribeRequestDTO;
import com.app.finance.service.WebSocketSessionService;
import com.app.finance.service.twelve.TwelveDataWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final TwelveDataWebSocketClient wsClient;
    private final WebSocketSessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/subscribe")
    public void subscribe(@Payload SubscribeRequestDTO request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (request.getSymbols() != null && !request.getSymbols().isEmpty()) {
            if ("unsubscribe".equalsIgnoreCase(request.getAction())) {
                request.getSymbols().forEach(s -> sessionService.unsubscribe(sessionId, s));
                wsClient.unsubscribe(request.getSymbols());
                log.info("Session {} unsubscribed from {}", sessionId, request.getSymbols());
            } else {
                request.getSymbols().forEach(s -> sessionService.subscribe(sessionId, s));
                wsClient.subscribe(request.getSymbols());
                log.info("Session {} subscribed to {}", sessionId, request.getSymbols());
            }
        }
    }

    @RequestMapping("/api/ws/status")
    @ResponseBody
    public Map<String, Object> wsStatus() {
        return Map.of(
                "connected", wsClient.isConnected(),
                "subscribedSymbols", wsClient.getSubscribedSymbols(),
                "activeSessions", sessionService.getActiveSessionCount()
        );
    }
}
