package com.example.quickpay.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌는 10개 입니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    USER_ACCOUNT_UN_MATCH("사용자와 계좌의 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지할 수 없습니다."),
    USER_NOT_FOUND("사용자가 없습니다.");
    private final String description;
}
