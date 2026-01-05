package com.moiez.pismo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @ManyToOne(optional = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(precision = 12, scale = SCALE, nullable = false)
    private BigDecimal amount;

    @Column(precision = 12, scale = SCALE, nullable = false)
    private BigDecimal balance;

    @Column
    private boolean settled;

    @CreationTimestamp
    @Column(nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;
}
