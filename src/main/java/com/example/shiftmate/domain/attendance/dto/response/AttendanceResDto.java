package com.example.shiftmate.domain.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AttendanceResDto {

    private Long attendanceId;
    private String status; // 정상출근, 지각, 퇴근 상태
    private String type; // 프론트에서 구분하기 위해 CLOCK-IN, CLOCK-OUT 타입 추가
    private LocalDateTime time; // 백엔드와 프론트의 시간이 다를 수 있기 때문에 백엔드에서 출퇴근 처리한 시간을 기준으로 하기 위해 시간 정보 추가
    private String message; // 프론트에서 보여줄 메시지
}
