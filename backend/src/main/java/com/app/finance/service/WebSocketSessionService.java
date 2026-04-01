package com.app.finance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketSessionService {

    // sessionId -> Set of subscribed symbols
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    // symbol -> Set of sessionIds
    private final Map<String, Set<String>> symbolSubscribers = new ConcurrentHashMap<>();

    public void subscribe(String sessionId, String symbol) {
        sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(symbol);
        symbolSubscribers.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        log.info("Session {} subscribed to {}", sessionId, symbol);
    }

    public void unsubscribe(String sessionId, String symbol) {
        Set<String> symbols = sessionSubscriptions.get(sessionId);
        if (symbols != null) symbols.remove(symbol);
        Set<String> sessions = symbolSubscribers.get(symbol);
        if (sessions != null) sessions.remove(symbol);
        log.info("Session {} unsubscribed from {}", sessionId, symbol);
    }

    public void removeSession(String sessionId) {
        Set<String> symbols = sessionSubscriptions.remove(sessionId);
        if (symbols != null) {
            symbols.forEach(symbol -> {
                Set<String> sessions = symbolSubscribers.get(symbol);
                if (sessions != null) sessions.remove(sessionId);
            });
        }
        log.info("Session {} removed", sessionId);
    }

    public Set<String> getSubscribedSymbols(String sessionId) {
        return sessionSubscriptions.getOrDefault(sessionId, Collections.emptySet());
    }

    public Set<String> getSubscribersForSymbol(String symbol) {
        return symbolSubscribers.getOrDefault(symbol, Collections.emptySet());
    }

    public Set<String> getAllSubscribedSymbols() {
        return symbolSubscribers.keySet();
    }

    public int getActiveSessionCount() {
        return sessionSubscriptions.size();
    }

    public boolean hasSubscribers(String symbol) {
        Set<String> subscribers = symbolSubscribers.get(symbol);
        return subscribers != null && !subscribers.isEmpty();
    }
}
