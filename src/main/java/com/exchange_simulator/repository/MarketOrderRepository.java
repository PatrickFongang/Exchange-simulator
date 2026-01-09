package com.exchange_simulator.repository;

import com.exchange_simulator.entity.MarketOrder;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {

    @Query(" select o from MarketOrder o where o.user.id = :userId " +
            " order by o.createdAt desc")
    List<MarketOrder> findAllByUserId(Long userId);
    @Query(" select o from MarketOrder o where o.user.id = :userId " +
            " and o.orderType = :orderType " +
            " order by o.createdAt desc")
    List<MarketOrder> findAllByOrderTypeAndUserId(OrderType orderType, Long userId);
    @Query("select o.createdAt from MarketOrder o " +
            " where o.user.id = :userId " +
            " and o.token = :token " +
            " and o.orderType = 'BUY' " +
            " order by o.createdAt desc limit 1")
    Instant getNewestOrderTimestamp(Long userId, String token);
    Long user(User user);

    BigDecimal token(String token);
}
