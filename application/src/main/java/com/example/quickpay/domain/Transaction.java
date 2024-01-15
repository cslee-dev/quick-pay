package com.example.quickpay.domain;

import com.example.quickpay.type.TransactionResultType;
import com.example.quickpay.type.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class Transaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;
    @ManyToOne
    private Account account;
    private Long amount;

    private Long balanceSnapshot;
    private String transactionId;
    private LocalDateTime transactedAt;


}
