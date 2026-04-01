package com.app.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trades_symbol", columnList = "symbol"),
        @Index(name = "idx_trades_trade_date", columnList = "trade_date"),
        @Index(name = "idx_trades_broker_id", columnList = "broker_id"),
        @Index(name = "idx_trades_stock_id", columnList = "stock_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType; // BUY or SELL

    @Column(name = "quantity", nullable = false, precision = 20, scale = 4)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 20, scale = 4)
    private BigDecimal price;

    @Column(name = "total_value", precision = 20, scale = 4)
    private BigDecimal totalValue;

    @Column(name = "trade_date", nullable = false)
    private LocalDateTime tradeDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Broker broker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stock stock;
}
