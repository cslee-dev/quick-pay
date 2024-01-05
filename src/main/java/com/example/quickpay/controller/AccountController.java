package com.example.quickpay.controller;

import com.example.quickpay.dto.AccountInfo;
import com.example.quickpay.dto.CreateAccount;
import com.example.quickpay.dto.DeleteAccount;
import com.example.quickpay.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(), request.getInitialBalance()
                )
        );
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(), request.getAccountNumber()
                )
        );
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(@RequestParam("user_id") Long userId) {
        return accountService.getAccountsByUserId(userId).stream()
                .map(AccountInfo::from)
                .collect(Collectors.toList());
    }

}
