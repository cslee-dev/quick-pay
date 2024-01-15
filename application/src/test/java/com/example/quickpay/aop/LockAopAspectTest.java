package com.example.quickpay.aop;

import com.example.quickpay.common.exception.QuickPayException;
import com.example.quickpay.dto.UseBalance;
import com.example.quickpay.service.LockService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.quickpay.common.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;

    @Test
    void lockAndUnLock() throws Throwable {
        //given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor = ArgumentCaptor.forClass(String.class);
        UseBalance.Request request = UseBalance.Request.builder()
                .userId(1L).accountNumber("1234567890").amount(1000L).build();
        //when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);
        //then
        verify(lockService, times(1)).lock(lockCaptor.capture());
        verify(lockService, times(1)).unlock(unLockCaptor.capture());

        assertEquals("1234567890", lockCaptor.getValue());
        assertEquals("1234567890", unLockCaptor.getValue());
    }

    @Test
    void lockAndUnLock_evenIfThrow() throws Throwable {
        //given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor = ArgumentCaptor.forClass(String.class);
        UseBalance.Request request = UseBalance.Request.builder()
                .userId(1L).accountNumber("1234567890").amount(1000L).build();
        given(proceedingJoinPoint.proceed())
                .willThrow(new QuickPayException(ACCOUNT_NOT_FOUND));
        //when
        assertThrows(QuickPayException.class,
                () -> lockAopAspect.aroundMethod(proceedingJoinPoint, request));
        //then
        verify(lockService, times(1)).lock(lockCaptor.capture());
        verify(lockService, times(1)).unlock(unLockCaptor.capture());

        assertEquals("1234567890", lockCaptor.getValue());
        assertEquals("1234567890", unLockCaptor.getValue());
    }
}