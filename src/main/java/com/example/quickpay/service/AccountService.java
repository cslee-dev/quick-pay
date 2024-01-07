package com.example.quickpay.service;

import com.example.quickpay.domain.Account;
import com.example.quickpay.domain.Member;
import com.example.quickpay.exception.QuickPayException;
import com.example.quickpay.repository.AccountRepository;
import com.example.quickpay.repository.MemberRepository;
import com.example.quickpay.service.dto.AccountDto;
import com.example.quickpay.type.AccountStatus;
import com.example.quickpay.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.quickpay.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌가 10개 있는지 조회
     * 계좌 번호를 생성하고
     * 계좌를 저장하고, 그 정보를 넘긴다
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        Member member = getMember(userId);
        validateCreateAccount(member);
        String newAccountNumber = createNewAccountNumber();
        Account account = accountRepository.save(createNewAccount(initialBalance, member, newAccountNumber));
        return AccountDto.fromEntity(account);
    }

    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new QuickPayException(USER_NOT_FOUND));
    }

    private void validateCreateAccount(Member member) {
        if (accountRepository.countByAccountUser(member).equals(10)) {
            throw new QuickPayException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    private String createNewAccountNumber() {
        return accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");
    }

    private Account createNewAccount(Long initialBalance, Member member, String newAccountNumber) {
        return Account.builder()
                .accountUser(member)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber(newAccountNumber)
                .balance(initialBalance)
                .registeredAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        Member member = getMember(userId);
        Account account = getAccount(accountNumber);

        validateDeleteAccount(member, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        return AccountDto.fromEntity(account);
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new QuickPayException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private void validateDeleteAccount(Member member, Account account) {
        if (!Objects.equals(member.getId(), account.getAccountUser().getId())) {
            throw new QuickPayException(USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new QuickPayException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new QuickPayException(BALANCE_NOT_EMPTY);
        }
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        Member member = getMember(userId);

        List<Account> accounts = accountRepository.findByAccountUser(member);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AccountDto getAccount(Long id) {
        return AccountDto.fromEntity(accountRepository.findById(id)
                .orElseThrow(() -> new QuickPayException(ACCOUNT_NOT_FOUND)
                ));
    }
}
