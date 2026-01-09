package com.exchange_simulator.entity;

import com.exchange_simulator.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.knowm.xchange.dto.Order;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Table(name = "marketOrders")
public class MarketOrder extends Base{

    public MarketOrder(String token, BigDecimal quantity, BigDecimal tokenPrice, BigDecimal orderValue, User user, OrderType orderType) {
        this.token = token;
        this.quantity = quantity;
        this.tokenPrice = tokenPrice;
        this.orderValue = orderValue;
        this.user = user;
        this.orderType = orderType;
    }
    @Getter
    @Column(nullable = false)
    private String token;

    @Getter
    @Column(nullable = false)
    private BigDecimal quantity;

    @Getter
    @Column(nullable = false)
    private BigDecimal tokenPrice;

    @Getter
    @Column(nullable = false)
    private BigDecimal orderValue;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;
    @Override
    public String toString() {
        return  "BuyOrder: " + quantity + " of " + token;
    }
}
