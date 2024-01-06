package com.example.quickpay.controller;

import com.example.quickpay.dto.CancelBalance;
import com.example.quickpay.dto.QueryTransactionResponse;
import com.example.quickpay.dto.UseBalance;
import com.example.quickpay.exception.QuickPayException;
import com.example.quickpay.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ) {
        try {
            return UseBalance.Response.from(
                    transactionService.useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount())
            );
        } catch (QuickPayException e) {
            log.error("Failed to use balance. ");
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Response request
    ){
        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(request.getTransactionId(), request.getAccountNumber(), request.getAmount())
            );
        } catch (QuickPayException e) {
            log.error("Failed to use balance. ");
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable("transactionId") String transactionId
    ) {
        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId)
        );
    }
}
