package com.example.shiftmate.domain.openShift.dto.response;

import com.example.shiftmate.domain.openShift.entity.OpenShiftRequest;
import com.example.shiftmate.domain.openShift.status.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class OpenShiftResDto {
    private Long id;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
    private RequestStatus requestStatus;
    private LocalDateTime createdAt;

    public static OpenShiftResDto from(OpenShiftRequest request) {
        return OpenShiftResDto.builder()
                .id(request.getId())
                .workDate(request.getWorkDate())
                .startTime(request.getShiftTemplate().getStartTime())
                .endTime(request.getShiftTemplate().getEndTime())
                .note(request.getNote())
                .requestStatus(request.getRequestStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
