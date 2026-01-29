package com.exchange_simulator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
@Data
public abstract class Base {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    protected Long id;

    @CreationTimestamp
    @Column(nullable = false)
    protected Instant createdAt;
}
