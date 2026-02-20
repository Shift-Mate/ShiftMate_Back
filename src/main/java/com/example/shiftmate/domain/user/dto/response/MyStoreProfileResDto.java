package com.example.shiftmate.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

// 선택한 스토어에서의 내 정보 응답 DTO
@Getter
@Builder
public class MyStoreProfileResDto { // 선택 스토어 상세 응답 클래스 시작

    private Long storeId; // 선택한 스토어 PK

    private String storeName; // 선택한 스토어 이름

    private String storeAlias; // 선택한 스토어 별칭

    private String role; // 매장 내 내 역할(MANAGER/STAFF)

    private String department; // 매장 내 내 부서(HALL/KITCHEN 등)

    private Integer hourlyWage; // 시급

    private Integer minHoursPerWeek; // 약속된 주간 최소 근무시간

    private String status; // 멤버 상태(ACTIVE/INVITED)

    private Long weeklyWorkedMinutes; // 이번 주 실제 근무 분(minute) 합계
} // 클래스 끝