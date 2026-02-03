package com.example.shiftmate.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    //Token
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다."),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰 형식이 올바르지 않습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰 형식입니다."),
    EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "토큰이 존재하지 않습니다."),

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // User
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // ShiftAssignment
    SHIFT_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "배정된 스케줄을 찾을 수 없습니다."),

    // Attendance
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "출근 기록을 찾을 수 없습니다."),
    ATTENDANCE_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 퇴근처리된 스케줄입니다."),
    INVALID_PIN_CODE(HttpStatus.UNAUTHORIZED, "핀번호가 일치하지 않습니다."),
    STORE_MISMATCH(HttpStatus.BAD_REQUEST, "해당 매장의 스케줄이 아닙니다.");

    private final HttpStatus status;
    private final String message;
}
