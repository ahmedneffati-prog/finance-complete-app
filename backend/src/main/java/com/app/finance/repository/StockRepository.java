package com.app.finance.repository;

import com.app.finance.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findByIsActiveTrue();
    List<Stock> findBySector(String sector);
    boolean existsBySymbol(String symbol);

    @Query("SELECT DISTINCT s.sector FROM Stock s WHERE s.sector IS NOT NULL ORDER BY s.sector")
    List<String> findDistinctSectors();

    @Query("SELECT DISTINCT s.exchange FROM Stock s WHERE s.exchange IS NOT NULL ORDER BY s.exchange")
    List<String> findDistinctExchanges();
}
