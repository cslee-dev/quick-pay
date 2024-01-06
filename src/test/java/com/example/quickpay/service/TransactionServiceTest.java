package com.example.quickpay.service;

import com.example.quickpay.domain.Account;
import com.example.quickpay.domain.Member;
import com.example.quickpay.domain.Transaction;
import com.example.quickpay.exception.QuickPayException;
import com.example.quickpay.repository.AccountRepository;
import com.example.quickpay.repository.MemberRepository;
import com.example.quickpay.repository.TransactionRepository;
import com.example.quickpay.service.dto.TransactionDto;
import com.example.quickpay.type.AccountStatus;
import com.example.quickpay.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.quickpay.type.AccountStatus.IN_USE;
import static com.example.quickpay.type.TransactionResultType.F;
import static com.example.quickpay.type.TransactionResultType.S;
import static com.example.quickpay.type.TransactionType.CANCEL;
import static com.example.quickpay.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("잔액 사용 성공")
    void useBalanceSuccess() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.useBalance(12L, "1000000012", 200L);
        verify(transactionRepository, times(1)).save(captor.capture());
        //then
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    void useBalanceFailed_UserNotFound() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.useBalance(1L, "1234567890", 200L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    void useBalanceFailed_AccountNotFound() {
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
                () -> transactionService.useBalance(1L, "1234567890", 200L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void useBalanceFailed_userUnMatch() {
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
                () -> transactionService.useBalance(1L, "1234567890", 200L));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 사용할 수 없다. - 잔앵 사용 실패")
    void useBalanceFailed_alreadyUnregistered() {
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
                () -> transactionService.useBalance(1L, "1234567890", 200L));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우 - 잔액 사용 실패")
    void useBalanceFailed_exceedAmount() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(100L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.useBalance(1L, "1234567890", 200L));
        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("실패 트랜잭션 저장 성공")
    void saveFailedUseTransactionSuccess() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        transactionService.saveFailedUseTransaction("1000000012", 200L);
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());
    }


    @Test
    @DisplayName("거래 취소 성공")
    void cancelBalanceSuccess() {
        //given
        Member member = Member.builder()
                .id(12L)
                .name("iron")
                .build();
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(0L)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .amount(100L)
                .balanceSnapshot(100L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .amount(100L)
                        .balanceSnapshot(100L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId", "1000000012", 100L);
        verify(transactionRepository, times(1)).save(captor.capture());
        //then
        assertEquals(100L, captor.getValue().getAmount());
        assertEquals(100L, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(100L, transactionDto.getAmount());
        assertEquals(100L, transactionDto.getBalanceSnapshot());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 거래 취소 실패")
    void cancelBalanceFailed_AccountNotFound() {
        //given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 200L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 거래 없음 - 거래 취소 실패")
    void cancelBalanceFailed_TransactionNotFound() {
        //given
        Member member = Member.builder()
                .id(15L)
                .name("iron")
                .build();
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(0L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.empty());
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 200L));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("거래와 계좌 매칭 실패 - 거래 취소 실패")
    void cancelBalanceFailed_TransactionAccountUnMatch() {
        //given
        Member member = Member.builder()
                .id(15L)
                .name("iron")
                .build();
        Account account = Account.builder()
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(0L)
                .build();
        Account accountNotUse = Account.builder()
                .id(1L)
                .accountUser(member)
                .accountStatus(IN_USE)
                .accountNumber("1000000013")
                .balance(0L)
                .build();
        Transaction transaction = Transaction.builder()
                .id(2L)
                .account(accountNotUse)
                .transactionType(USE)
                .transactionResultType(S)
                .amount(100L)
                .balanceSnapshot(100L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.of(transaction));
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 200L));
        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액과 취소 금액이 다름 - 거래 취소 실패")
    void cancelBalanceFailed_CancelMustFully() {
        //given
        Member member = Member.builder()
                .id(12L).name("iron").build();
        Account account = Account.builder()
                .accountUser(member).accountStatus(IN_USE).accountNumber("1000000012")
                .balance(0L).build();
        Transaction transaction = Transaction.builder()
                .id(2L)
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .amount(100L)
                .balanceSnapshot(100L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.of(transaction));

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.cancelBalance("", "1234567890", 200L));
        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }


    @Test
    @DisplayName("거래 기간이 1년 지난 경우 - 거래 취소 실패")
    void cancelBalanceFailed_TooOldTransactionToCancel() {
        //given
        Member member = Member.builder()
                .id(12L).name("iron").build();
        Account account = Account.builder()
                .accountUser(member).accountStatus(IN_USE).accountNumber("1000000012")
                .balance(0L).build();
        Transaction transaction = Transaction.builder()
                .id(2L)
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .amount(100L)
                .balanceSnapshot(100L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1))
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.of(transaction));

        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.cancelBalance("", "1234567890", 100L));
        //then
        assertEquals(ErrorCode.TOO_OLD_TRANSACTION_TO_CANCEL, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 조회 성공")
    void queryTransactionSuccess() {
        Member member = Member.builder()
                .id(12L).name("iron").build();
        Account account = Account.builder()
                .accountUser(member).accountStatus(IN_USE).accountNumber("1000000012")
                .balance(0L).build();
        Transaction transaction = Transaction.builder()
                .id(2L)
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .amount(100L)
                .balanceSnapshot(100L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1))
                .build();
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        TransactionDto transactionDto = transactionService.queryTransaction("transactionId");
        //then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(100L, transactionDto.getBalanceSnapshot());
        assertEquals(100L, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("해당 거래 없음 - 거래 조회 실패")
    void queryTransactionFailed_TransactionNotFound() {
        //given

        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.empty());
        //when
        QuickPayException exception = assertThrows(QuickPayException.class,
                () -> transactionService.queryTransaction("transactionId"));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}