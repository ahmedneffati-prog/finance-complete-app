package com.app.finance.repository;

import com.app.finance.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    List<Trade> findBySymbol(String symbol);

    List<Trade> findByTradeDateBetween(LocalDateTime start, LocalDateTime end);

    List<Trade> findByBrokerId(Long brokerId);

    List<Trade> findByStockId(Long stockId);

    @Query("SELECT t FROM Trade t WHERE t.tradeDate BETWEEN :start AND :end ORDER BY t.tradeDate DESC")
    List<Trade> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t.symbol, SUM(t.totalValue) as totalValue, COUNT(t) as tradeCount " +
           "FROM Trade t GROUP BY t.symbol ORDER BY totalValue DESC")
    List<Object[]> findSymbolSummary();

    @Query("SELECT t.tradeType, SUM(t.totalValue), AVG(t.price), COUNT(t) FROM Trade t GROUP BY t.tradeType")
    List<Object[]> findTradeTypeSummary();
}
