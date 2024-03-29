package com.example.quickpay.controller;

import com.example.quickpay.dto.CancelBalance;
import com.example.quickpay.dto.UseBalance;
import com.example.quickpay.service.TransactionService;
import com.example.quickpay.service.dto.TransactionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static com.example.quickpay.common.type.TransactionResultType.SUCCESS;
import static com.example.quickpay.common.type.TransactionType.USE;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("거래 사용 성공")
    void successUseBalance() throws Exception {
        //given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234567890")
                        .amount(1234L)
                        .transactedAt(LocalDateTime.now())
                        .transactionType(USE)
                        .transactionResultType(SUCCESS)
                        .transactionId("transactionId").build());
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UseBalance.Request(1L, "1234567890", 1000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(1234L))
                .andExpect(jsonPath("$.transactionResult").value("SUCCESS"));
    }

    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() throws Exception {
        //given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234567890")
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .transactionResultType(SUCCESS)
                        .transactionId("transactionId").build());
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelBalance.Request("transactionId", "1234567890", 1000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(1000L))
                .andExpect(jsonPath("$.transactionResult").value("SUCCESS"));
    }

    @Test
    @DisplayName("거래 조회 성공")
    void successQueryTransaction() throws Exception {
        //given
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234567890")
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .transactionResultType(SUCCESS)
                        .transactionId("transactionId").build());
        //when
        //then
        mockMvc.perform(get("/api/v1/transaction/transactionId"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(1000L))
                .andExpect(jsonPath("$.transactionResult").value("SUCCESS"));
    }
}