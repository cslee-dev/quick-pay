package com.example.quickpay.service;

import com.example.quickpay.common.exception.QuickPayException;
import com.example.quickpay.common.type.AccountStatus;
import com.example.quickpay.common.type.ErrorCode;
import com.example.quickpay.domain.mysql.entity.Account;
import com.example.quickpay.domain.mysql.entity.Member;
import com.example.quickpay.domain.mysql.repository.AccountRepository;
import com.example.quickpay.domain.mysql.repository.MemberRepository;
import com.example.quickpay.service.dto.AccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(member)
                        .accountNumber("1000000013")
                        .build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("첫 계좌 생성 성공")
    void createFirstAccountSuccess() {
        //given
        Member member = Member.builder()
                .id(15L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(member)
                        .accountNumber("1000000013")
                        .build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저당 최대 계좌 10개 - 계좌 생성 실패")
    void createAccount_maxAccountIs10() {
        //given
        Member member = Member.builder()
                .id(15L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.createAccount(1L, 1000L));
        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 생성 성공")
    void deleteAccountSuccess() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(member)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));
        //when
        AccountDto accountDto = accountService.deleteAccount(member.getId(), "1000000012");
        //then
        assertEquals(12L, accountDto.getUserId());
    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        Member member = Member.builder()
                .id(15L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 계좌 해지 실패")
    void deleteAccountFailed_userUnMatch() {
        //given
        Member member = Member.builder()
                .id(15L).name("iron").build();
        Member member1 = Member.builder()
                .id(12L).name("haven").build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(member1)
                        .accountNumber("1000000013")
                        .build()));
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다 - 계좌 해지 실패")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        Member member = Member.builder()
                .id(15L).name("iron").build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(member)
                        .balance(1000L)
                        .accountNumber("1000000013")
                        .build()));
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 해지할 수 없다 - 계좌 해지 실패")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        Member member = Member.builder()
                .id(15L).name("iron").build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(member)
                        .balance(0L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("1000000013")
                        .build()));
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }


    @Test
    @DisplayName("사용자 계좌 리스트 조회 성공")
    void successGetAccountsByUserId() {
        //given
        Member member = Member.builder()
                .id(15L).name("iron").build();
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(member)
                        .accountNumber("1111111111")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(member)
                        .accountNumber("4444444444")
                        .balance(3000L)
                        .build(),
                Account.builder()
                        .accountUser(member)
                        .accountNumber("3333333333")
                        .balance(4000L)
                        .build(),
                Account.builder()
                        .accountUser(member)
                        .accountNumber("2222222222")
                        .balance(5000L)
                        .build()
        );
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(member.getId());
        //then
        assertEquals(4, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(2000L, accountDtos.get(0).getBalance());
        assertEquals("4444444444", accountDtos.get(1).getAccountNumber());
        assertEquals(3000L, accountDtos.get(1).getBalance());
        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(4000L, accountDtos.get(2).getBalance());
        assertEquals("2222222222", accountDtos.get(3).getAccountNumber());
        assertEquals(5000L, accountDtos.get(3).getBalance());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 리스트 조회 실패")
    void failedGetAccountsByUserId_userNotFound() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> accountService.getAccountsByUserId(1L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}