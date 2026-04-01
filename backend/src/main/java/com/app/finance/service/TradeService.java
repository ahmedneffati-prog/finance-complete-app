package com.app.finance.service;

import com.app.finance.dto.request.TradeRequestDTO;
import com.app.finance.dto.response.TradeResponseDTO;
import com.app.finance.entity.Broker;
import com.app.finance.entity.Stock;
import com.app.finance.entity.Trade;
import com.app.finance.exception.ResourceNotFoundException;
import com.app.finance.repository.BrokerRepository;
import com.app.finance.repository.StockRepository;
import com.app.finance.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;
    private final BrokerRepository brokerRepository;
    private final StockRepository stockRepository;

    public List<TradeResponseDTO> getAllTrades() {
        return tradeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<TradeResponseDTO> getTradesPaged(Pageable pageable) {
        return tradeRepository.findAll(pageable).map(this::toDTO);
    }

    public TradeResponseDTO getTradeById(Long id) {
        return toDTO(tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", id)));
    }

    public List<TradeResponseDTO> getTradesBySymbol(String symbol) {
        return tradeRepository.findBySymbol(symbol).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TradeResponseDTO> getTradesByDateRange(LocalDateTime start, LocalDateTime end) {
        return tradeRepository.findByDateRange(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TradeResponseDTO> getTradesByBroker(Long brokerId) {
        return tradeRepository.findByBrokerId(brokerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TradeResponseDTO createTrade(TradeRequestDTO dto) {
        validateTradeType(dto.getTradeType());

        Trade trade = Trade.builder()
                .symbol(dto.getSymbol().toUpperCase())
                .tradeType(dto.getTradeType().toUpperCase())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .totalValue(dto.getQuantity().multiply(dto.getPrice()))
                .tradeDate(dto.getTradeDate() != null ? dto.getTradeDate() : LocalDateTime.now())
                .build();

        if (dto.getBrokerId() != null) {
            Broker broker = brokerRepository.findById(dto.getBrokerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Broker", dto.getBrokerId()));
            trade.setBroker(broker);
        }

        if (dto.getStockId() != null) {
            Stock stock = stockRepository.findById(dto.getStockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock", dto.getStockId()));
            trade.setStock(stock);
        }

        return toDTO(tradeRepository.save(trade));
    }

    @Transactional
    public TradeResponseDTO updateTrade(Long id, TradeRequestDTO dto) {
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", id));
        validateTradeType(dto.getTradeType());

        trade.setSymbol(dto.getSymbol().toUpperCase());
        trade.setTradeType(dto.getTradeType().toUpperCase());
        trade.setQuantity(dto.getQuantity());
        trade.setPrice(dto.getPrice());
        trade.setTotalValue(dto.getQuantity().multiply(dto.getPrice()));
        if (dto.getTradeDate() != null) trade.setTradeDate(dto.getTradeDate());

        if (dto.getBrokerId() != null) {
            Broker broker = brokerRepository.findById(dto.getBrokerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Broker", dto.getBrokerId()));
            trade.setBroker(broker);
        }

        if (dto.getStockId() != null) {
            Stock stock = stockRepository.findById(dto.getStockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock", dto.getStockId()));
            trade.setStock(stock);
        }

        return toDTO(tradeRepository.save(trade));
    }

    @Transactional
    public void deleteTrade(Long id) {
        if (!tradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trade", id);
        }
        tradeRepository.deleteById(id);
    }

    private void validateTradeType(String tradeType) {
        if (!"BUY".equalsIgnoreCase(tradeType) && !"SELL".equalsIgnoreCase(tradeType)) {
            throw new IllegalArgumentException("Trade type must be BUY or SELL");
        }
    }

    public TradeResponseDTO toDTO(Trade trade) {
        return TradeResponseDTO.builder()
                .id(trade.getId())
                .symbol(trade.getSymbol())
                .tradeType(trade.getTradeType())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .totalValue(trade.getTotalValue())
                .tradeDate(trade.getTradeDate())
                .createdAt(trade.getCreatedAt())
                .brokerId(trade.getBroker() != null ? trade.getBroker().getId() : null)
                .brokerName(trade.getBroker() != null ? trade.getBroker().getName() : null)
                .stockId(trade.getStock() != null ? trade.getStock().getId() : null)
                .stockName(trade.getStock() != null ? trade.getStock().getName() : null)
                .build();
    }
}
