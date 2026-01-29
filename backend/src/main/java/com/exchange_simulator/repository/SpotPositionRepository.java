package com.exchange_simulator.repository;

import com.exchange_simulator.entity.SpotPosition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SpotPositionRepository extends JpaRepository<SpotPosition, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from SpotPosition p where p.user.id = :userId")
    List<SpotPosition> findAllByUserIdWithLock(Long userId);

    @Query("select p from SpotPosition p where p.user.id = :userId")
    List<SpotPosition> findAllByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("update SpotPosition p set p.avgBuyPrice = (" +
            "select sum(o.tokenPrice * o.quantity) / sum(o.quantity) from Order o " +
            "where o.user.id = :userId " +
            "and o.token = :token " +
            "and o.transactionType = 'BUY' " +
            "and o.closedAt is not null " +
            "and (o.closedAt >= p.timestamp " +
            " or o.createdAt >= p.timestamp)) " +
            "where p.user.id = :userId and p.id = :posId")
    void updateAvgBuyPriceByUserAndPositionId(Long userId, Long posId, String token);

    Optional<SpotPosition> findByUserIdAndToken(Long userId, String token);
}
