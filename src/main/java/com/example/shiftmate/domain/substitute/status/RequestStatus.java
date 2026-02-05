package com.example.shiftmate.domain.substitute.status;

public enum RequestStatus {
    OPEN, // 대타 요청이 등록되었으며 수락자가 없는 상태
    PENDING, // 수락자가 생겨 관리자 승인을 기다리는 상태
    APPROVED, // 관리자가 승인하여 근무 스케줄 변경이 확정된 상태
    REQUESTER_CANCELED, // 요청자가 대타 요청을 취소한 상태
    MANAGER_CANCELLED  // 관리자가 대타 요청을 취소한 상태
}
