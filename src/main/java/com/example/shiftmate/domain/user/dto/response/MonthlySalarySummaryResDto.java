package com.example.shiftmate.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 선택한 월의 급여 집계 전체 응답 DTO
@Getter
@Builder
public class MonthlySalarySummaryResDto {

    // 조회 기준 년/월
    private int year;
    private int month;

    // 해당 월 전체 스토어 예상 급여 합계
    private Long totalEstimatedPay;

    // 스토어별 상세 집계 목록
    private List<StoreMonthlySalaryResDto> stores;
}