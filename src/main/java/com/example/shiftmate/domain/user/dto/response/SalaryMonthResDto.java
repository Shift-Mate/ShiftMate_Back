package com.example.shiftmate.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 월 필터(선택 가능한 년/월) 한 건을 표현하는 DTO
@Getter
@AllArgsConstructor
public class SalaryMonthResDto {

    // 예: 2025
    private int year;

    // 예: 11 (1~12)
    private int month;
}