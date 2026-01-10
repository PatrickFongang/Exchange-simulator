package com.exchange_simulator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="spotPosition")
public class SpotPosition extends Base{
    public SpotPosition() {}

    public SpotPosition(String token, BigDecimal quantity, BigDecimal avgBuyPrice, User user, Instant lastBuyOrder) {
        this.token = token;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.timestamp = lastBuyOrder;
        this.user = user;
    }

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Column(nullable = false)
    private String token;

    @Getter
    @Setter
    @Column(nullable = false)
    private BigDecimal quantity;

    @Getter
    @Column(nullable = false)
    private BigDecimal avgBuyPrice;

    @Getter
    @Setter
    @Column(nullable = false)
    private Instant timestamp;

    @Override
    public String toString() {
        return quantity + " of " + token;
    }
}
