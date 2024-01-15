package com.example.quickpay.controller;

import com.example.quickpay.dto.CreateAccount;
import com.example.quickpay.dto.DeleteAccount;
import com.example.quickpay.exception.QuickPayException;
import com.example.quickpay.service.AccountService;
import com.example.quickpay.service.dto.AccountDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.example.quickpay.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() throws Exception {
        //given
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when
        //then
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAccount.Request(1L, 100L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void successDeleteAccount() throws Exception {
        //given
        AccountDto accountDto = AccountDto.builder()
                .userId(1L)
                .accountNumber("1234567890")
                .registeredAt(LocalDateTime.now())
                .unRegisteredAt(LocalDateTime.now())
                .build();
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(accountDto);
        //when
        //then
        mockMvc.perform(delete("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(accountDto.getUserId(), accountDto.getAccountNumber())
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 계좌 리스트 조회 성공")
    void successGetAccountsByUserId() throws Exception {
        //given
        AccountDto accountDto = AccountDto.builder()
                .userId(1L)
                .accountNumber("1234567890")
                .balance(1000L)
                .registeredAt(LocalDateTime.now())
                .unRegisteredAt(LocalDateTime.now())
                .build();
        AccountDto accountDto2 = AccountDto.builder()
                .userId(1L)
                .balance(100L)
                .accountNumber("1234567891")
                .registeredAt(LocalDateTime.now())
                .unRegisteredAt(LocalDateTime.now())
                .build();
        List<AccountDto> accountDtos = Arrays.asList(accountDto, accountDto2);

        given(accountService.getAccountsByUserId(anyLong()))
                .willReturn(accountDtos);
        //when
        //then
        mockMvc.perform(get("/api/v1/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber").value(accountDto.getAccountNumber()))
                .andExpect(jsonPath("$[0].balance").value(accountDto.getBalance()));
    }

    @Test
    @DisplayName("계좌 아이디 조회 실패")
    void failedGetAccount() throws Exception {
        //given
        given(accountService.getAccount(anyLong()))
                .willThrow(new QuickPayException(ACCOUNT_NOT_FOUND));
        //then
        mockMvc.perform(get("/api/v1/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("계좌가 없습니다."))
                .andExpect(status().isOk());
    }
}