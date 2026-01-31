package com.example.shiftmate.domain.substitute.status;

public enum ApplicationStatus {
    WAITING, // 수락자가 관리자의 승인을 기다리는 상태
    SELECTED, // 관리자가 대타 근무자를 선택한 상태, 이때 RequestStatus가 APPROVED로 변경
    REJECTED, // 관리자가 거절한 상태 또는 선착순에서 밀린 상태
    CANCELED // 지원자가 지원을 취소한 상태
}