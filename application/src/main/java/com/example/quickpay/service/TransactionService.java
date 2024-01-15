package com.example.quickpay.service;

import com.example.quickpay.common.exception.QuickPayException;
import com.example.quickpay.common.type.AccountStatus;
import com.example.quickpay.common.type.ErrorCode;
import com.example.quickpay.common.type.TransactionResultType;
import com.example.quickpay.common.type.TransactionType;
import com.example.quickpay.domain.mysql.entity.Account;
import com.example.quickpay.domain.mysql.entity.Member;
import com.example.quickpay.domain.mysql.entity.Transaction;
import com.example.quickpay.domain.mysql.repository.AccountRepository;
import com.example.quickpay.domain.mysql.repository.MemberRepository;
import com.example.quickpay.domain.mysql.repository.TransactionRepository;
import com.example.quickpay.service.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.quickpay.common.type.TransactionResultType.FAILED;
import static com.example.quickpay.common.type.TransactionResultType.SUCCESS;
import static com.example.quickpay.common.type.TransactionType.CANCEL;
import static com.example.quickpay.common.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        Member member = getMember(userId);
        Account account = getAccount(accountNumber);
        validateUseBalance(member, account, amount);
        account.useBalance(amount);
        return TransactionDto.fromEntity(saveTransaction(USE, SUCCESS, account, amount));
    }

    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {
        Account account = getAccount(accountNumber);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new QuickPayException(ErrorCode.TRANSACTION_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);
        return TransactionDto.fromEntity(saveTransaction(CANCEL, SUCCESS, account, amount));
    }


    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new QuickPayException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUseBalance(Member member, Account account, Long amount) {
        if (!Objects.equals(member.getId(), account.getAccountUser().getId())) {
            throw new QuickPayException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new QuickPayException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new QuickPayException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = getAccount(accountNumber);
        saveTransaction(USE, FAILED, account, amount);
    }

    private Transaction saveTransaction(TransactionType transactionType, TransactionResultType transactionResultType, Account account, Long amount) {
        return transactionRepository.save(Transaction.builder()
                .transactionType(transactionType)
                .transactionResultType(transactionResultType)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());
    }


    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new QuickPayException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new QuickPayException(ErrorCode.CANCEL_MUST_FULLY);
        }
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new QuickPayException(ErrorCode.TOO_OLD_TRANSACTION_TO_CANCEL);

        }
    }

    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = getAccount(accountNumber);
        saveTransaction(CANCEL, FAILED, account, amount);
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new QuickPayException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public TransactionDto queryTransaction(String transactionId) {
        return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new QuickPayException(ErrorCode.TRANSACTION_NOT_FOUND)));

    }
}
