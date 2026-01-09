package com.exchange_simulator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
public abstract class Base {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    @Getter
    protected Long id;

    @CreationTimestamp
    @Column(nullable = false)
    @Getter
    protected Instant createdAt;
}
