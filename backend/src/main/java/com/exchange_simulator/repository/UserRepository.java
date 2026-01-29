package com.exchange_simulator.repository;

import com.exchange_simulator.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithLock(Long id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("update User u set u.username = :name where u.id = :id")
    int updateUsernameById(Long id, String username);

    @Transactional
    @Modifying
    @Query("update User u set u.email = :email where u.id = :id")
    int updateEmailById (Long id, String email);

    Optional<User> findByUsername(String username);
}