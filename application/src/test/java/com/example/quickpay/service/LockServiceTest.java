package com.example.quickpay.service;

import com.example.quickpay.common.exception.QuickPayException;
import com.example.quickpay.common.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rlock;

    @InjectMocks
    private LockService lockService;
    
    @Test
    void successGetLock () throws InterruptedException {
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rlock);
        given(rlock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);
        //when
        //then
        assertDoesNotThrow(() -> lockService.lock("1234567890"));
     }

    @Test
    void failedGetLock () throws InterruptedException {
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rlock);
        given(rlock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false);
        //when
        QuickPayException exception = assertThrows(QuickPayException.class, () -> lockService.lock("1234567890"));

        //then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_TRANSACTION_LOCK);
    }
    
}