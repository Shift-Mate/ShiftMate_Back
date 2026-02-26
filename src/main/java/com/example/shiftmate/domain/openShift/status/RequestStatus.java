package com.example.shiftmate.domain.openShift.status;

public enum RequestStatus {
    OPEN, // 생성됨
    RECRUITING, // 모집중
    CLOSED,     // 마감됨 (매칭 완료)
    CANCELED // 취소됨
}
