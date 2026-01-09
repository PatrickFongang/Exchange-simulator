package com.exchange_simulator.repository;

import com.exchange_simulator.entity.SpotPosition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface SpotPositionRepository extends JpaRepository<SpotPosition, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from SpotPosition p where p.user.id = :userId")
    List<SpotPosition> findAllByUserIdWithLock(Long userId);

    @Query("select p from SpotPosition p where p.user.id = :userId")
    List<SpotPosition> findAllByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("update SpotPosition p set p.avgBuyPrice = (" +
            "select sum(o.tokenPrice * o.quantity) / sum(o.quantity) from MarketOrder o " +
            "WHERE o.user.id = :userId " +
            "AND o.token = :token " +
            "AND o.orderType = 'BUY' " +
            "AND o.createdAt >= p.timestamp) " +
            "WHERE p.user.id = :userId AND p.id = :posId")
    void updateAvgBuyPriceByUserAndPositionId (Long userId, Long posId, String token);

}
