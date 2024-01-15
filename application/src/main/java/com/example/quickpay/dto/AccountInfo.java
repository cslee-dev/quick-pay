package com.example.quickpay.dto;

import com.example.quickpay.service.dto.AccountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountInfo {
    private String accountNumber;
    private Long balance;


    public static AccountInfo from(AccountDto accountDto) {
        return builder()
                .accountNumber(accountDto.getAccountNumber())
                .balance(accountDto.getBalance())
                .build();
    }

}
