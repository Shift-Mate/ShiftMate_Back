package com.example.shiftmate.domain.substitute.dto.response;

import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
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
public class SubstituteResDto {

    private Long shiftAssignmentId;
    private Long requestId;

    // 대타 요청자 정보
    private Long requesterId;
    private String requesterName;

    // 스케줄 정보
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String shiftName;

    // 대타 요청 정보
    private RequestStatus status;
    private String reason;
    private LocalDateTime createdAt;

    public static SubstituteResDto from(SubstituteRequest request) {
        return SubstituteResDto.builder()
                .shiftAssignmentId(request.getShiftAssignment().getId())
                .requestId(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getUser().getName())
                .workDate(request.getShiftAssignment().getWorkDate())
                .startTime(request.getShiftAssignment().getShiftTemplate().getStartTime())
                .endTime(request.getShiftAssignment().getShiftTemplate().getEndTime())
                .shiftName(request.getShiftAssignment().getShiftTemplate().getName())
                .status(request.getStatus())
                .reason(request.getReason())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
