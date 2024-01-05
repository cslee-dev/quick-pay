package com.example.quickpay.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌는 10개 입니다."),
    USER_NOT_FOUND("사용자가 없습니다.");
    private final String description;
}
