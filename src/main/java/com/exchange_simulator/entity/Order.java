package com.exchange_simulator.entity;

import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@NoArgsConstructor
@Table(name = "marketOrders")
public class Order extends Base{

    public Order(String token, BigDecimal quantity, BigDecimal tokenPrice,
                 BigDecimal orderValue, User user, TransactionType transactionType,
                 OrderType orderType, Instant closedAt) {
        this.token = token;
        this.quantity = quantity;
        this.tokenPrice = tokenPrice;
        this.orderValue = orderValue;
        this.user = user;
        this.transactionType = transactionType;
        this.orderType = orderType;
        this.closedAt = closedAt;
    }
    @Getter
    @Column(nullable = false)
    private String token;

    @Getter
    @Column(nullable = false, name = "quantity", precision = 19, scale = 8)
    private BigDecimal quantity;

    @Getter
    @Column(nullable = false)
    private BigDecimal tokenPrice;

    @Getter
    @Column(nullable = false, name = "orderValue", precision = 19, scale = 8)
    private BigDecimal orderValue;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Getter
    @Setter
    @Column
    private Instant closedAt;

    @Override
    public String toString() {
        return  "BuyOrder: " + quantity + " of " + token;
    }
}
