package com.example.quickpay.common.exception;

import com.example.quickpay.common.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// transaction 롤백에 해당되지 않음
// unchecked
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuickPayException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMessage;

    public QuickPayException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
