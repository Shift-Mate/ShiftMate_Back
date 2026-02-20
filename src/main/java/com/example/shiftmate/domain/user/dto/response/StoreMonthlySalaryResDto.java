package com.example.shiftmate.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

// 선택한 월에서 "스토어 1개" 기준 급여 집계 응답 DTO
@Getter
@Builder
public class StoreMonthlySalaryResDto {

    // 스토어 식별자
    private Long storeId;

    // 스토어 이름 (예: 강남점)
    private String storeName;

    // 스토어 별칭 (없을 수 있음)
    private String storeAlias;

    // 해당 월 계산에 사용된 시급
    private Integer hourlyWage;

    // 해당 월 총 근무 분
    private Long workedMinutes;

    // 화면 표시 편의를 위한 시간 단위(분 -> 시간)
    private Long workedHours;

    // 해당 월 예상 급여 (원)
    private Long estimatedPay;
}