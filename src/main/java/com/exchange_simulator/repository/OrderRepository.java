package com.exchange_simulator.repository;

import com.exchange_simulator.entity.Order;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(" select o from Order o where o.user.id = :userId " +
            " order by o.createdAt desc")
    List<Order> findAllByUserId(Long userId);

    @Query("select o from Order o " +
            " where o.user.id = :userId " +
            " and o.id = :orderId")
    Optional<Order> findByOrderAndUserId(Long userId, Long orderId);
    @Query(" select o from Order o where o.user.id = :userId " +
            " and o.transactionType = :transactionType " +
            " order by o.createdAt desc")
    List<Order> findAllByOrderTypeAndUserId(TransactionType transactionType, Long userId);
    @Query("select o.createdAt from Order o " +
            " where o.user.id = :userId " +
            " and o.token = :token " +
            " and o.transactionType = 'BUY' " +
            " order by o.createdAt desc limit 1")
    Instant getNewestOrderTimestamp(Long userId, String token);
    Long user(User user);

    BigDecimal token(String token);

    List<Order> findByClosedAtIsNull();

    @Query("select o from Order o " +
            " where o.user.id = :userId " +
            " and o.closedAt IS NULL " +
            " and o.token = :token " +
            " and o.transactionType = 'SELL'")
    List<Order> findOpenSellOrdersByUserAndToken(Long userId, String token);
}
