package com.app.finance.service;

import com.app.finance.dto.request.StockRequestDTO;
import com.app.finance.dto.response.StockResponseDTO;
import com.app.finance.entity.Stock;
import com.app.finance.exception.ResourceNotFoundException;
import com.app.finance.repository.StockRepository;
import com.app.finance.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    @Cacheable(Constants.CACHE_STOCKS)
    public List<StockResponseDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<StockResponseDTO> getActiveStocks() {
        return stockRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public StockResponseDTO getStockById(Long id) {
        return toDTO(stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id)));
    }

    public StockResponseDTO getStockBySymbol(String symbol) {
        return toDTO(stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol)));
    }

    public List<StockResponseDTO> getStocksBySector(String sector) {
        return stockRepository.findBySector(sector).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllSectors() {
        return stockRepository.findDistinctSectors();
    }

    public List<String> getAllExchanges() {
        return stockRepository.findDistinctExchanges();
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_STOCKS, allEntries = true)
    public StockResponseDTO createStock(StockRequestDTO dto) {
        if (stockRepository.existsBySymbol(dto.getSymbol())) {
            throw new IllegalArgumentException("Stock with symbol '" + dto.getSymbol() + "' already exists");
        }
        Stock stock = Stock.builder()
                .symbol(dto.getSymbol().toUpperCase())
                .name(dto.getName())
                .sector(dto.getSector())
                .exchange(dto.getExchange())
                .currency(dto.getCurrency())
                .marketCap(dto.getMarketCap())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        return toDTO(stockRepository.save(stock));
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_STOCKS, allEntries = true)
    public StockResponseDTO updateStock(Long id, StockRequestDTO dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));
        stock.setSymbol(dto.getSymbol().toUpperCase());
        stock.setName(dto.getName());
        stock.setSector(dto.getSector());
        stock.setExchange(dto.getExchange());
        stock.setCurrency(dto.getCurrency());
        stock.setMarketCap(dto.getMarketCap());
        if (dto.getIsActive() != null) stock.setIsActive(dto.getIsActive());
        return toDTO(stockRepository.save(stock));
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_STOCKS, allEntries = true)
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock", id);
        }
        stockRepository.deleteById(id);
    }

    public StockResponseDTO toDTO(Stock stock) {
        return StockResponseDTO.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .sector(stock.getSector())
                .exchange(stock.getExchange())
                .currency(stock.getCurrency())
                .marketCap(stock.getMarketCap())
                .isActive(stock.getIsActive())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
