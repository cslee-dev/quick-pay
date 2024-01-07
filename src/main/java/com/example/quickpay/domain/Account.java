package com.example.quickpay.domain;


import com.example.quickpay.exception.QuickPayException;
import com.example.quickpay.type.AccountStatus;
import com.example.quickpay.type.ErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Account extends BaseEntity {

    @ManyToOne
    private Member accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;


    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new QuickPayException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new QuickPayException(ErrorCode.INVALID_REQUEST);
        }
        balance += amount;
    }
}
