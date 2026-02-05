package com.example.shiftmate.domain.substitute.dto.response;

import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubstituteApplicationResDto {
    Long applicationId;
    Long requestId;

    // 대타 스케줄 정보
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String shiftName;

    // 요청자 정보
    private Long requesterId;
    private String requesterName;

    // 지원자 정보
    private Long applicantId;
    private String applicantName;

    // 대타 요청 상태
    private RequestStatus requestStatus;
    private ApplicationStatus applicationStatus;

    // 지원 날짜
    private LocalDateTime createdAt;

    public static SubstituteApplicationResDto from(SubstituteApplication application) {
        return SubstituteApplicationResDto.builder()
                .applicationId(application.getId())
                .requestId(application.getRequest().getId())
                .workDate(application.getRequest().getShiftAssignment().getWorkDate())
                .startTime(application.getRequest().getShiftAssignment().getShiftTemplate().getStartTime())
                .endTime(application.getRequest().getShiftAssignment().getShiftTemplate().getEndTime())
                .shiftName(application.getRequest().getShiftAssignment().getShiftTemplate().getName())
                .applicantId(application.getApplicant().getId())
                .applicantName(application.getApplicant().getUser().getName())
                .requesterId(application.getRequest().getRequester().getId())
                .requesterName(application.getRequest().getRequester().getUser().getName())
                .requestStatus(application.getRequest().getStatus())
                .applicationStatus(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
