package com.exchange_simulator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
public class User extends Base{
    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.DETACH)
    private final List<SpotPosition> positions = new ArrayList<>();

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Setter
    @Column(nullable = false)
    private String password;

    @Getter
    @Setter
    @Column(nullable = false)
    private String role;

    @Getter
    @Setter
    @Column(nullable = false)
    private BigDecimal funds = BigDecimal.valueOf(1000.0);

    @UpdateTimestamp
    @Column(nullable = false)
    @Getter
    private Instant updatedAt;

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public String toString() {
        return "User " + username + " with email = " + email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(positions, user.positions) && Objects.equals(username, user.username) && Objects.equals(email, user.email) && Objects.equals(funds, user.funds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions, username, email, funds);
    }
}
