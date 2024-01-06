package com.example.quickpay.service;

import com.example.quickpay.domain.Account;
import com.example.quickpay.domain.Member;
import com.example.quickpay.domain.Transaction;
import com.example.quickpay.exception.AccountException;
import com.example.quickpay.repository.AccountRepository;
import com.example.quickpay.repository.MemberRepository;
import com.example.quickpay.repository.TransactionRepository;
import com.example.quickpay.service.dto.TransactionDto;
import com.example.quickpay.type.AccountStatus;
import com.example.quickpay.type.ErrorCode;
import com.example.quickpay.type.TransactionResultType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.quickpay.type.TransactionResultType.F;
import static com.example.quickpay.type.TransactionResultType.S;
import static com.example.quickpay.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateUseBalance(member, account, amount);
        account.useBalance(amount);
        return TransactionDto.fromEntity(saveUseTransaction(S, account, amount));
    }

    private void validateUseBalance(Member member, Account account, Long amount) {
        if (!Objects.equals(member.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        saveUseTransaction(F, account, amount);
    }

    private Transaction saveUseTransaction(TransactionResultType transactionResultType, Account account, Long amount) {
        return transactionRepository.save(Transaction.builder()
                .transactionType(USE)
                .transactionResultType(transactionResultType)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());
    }

}
