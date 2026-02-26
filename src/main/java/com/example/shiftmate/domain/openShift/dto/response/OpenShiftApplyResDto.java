package com.example.shiftmate.domain.openShift.dto.response;

import com.example.shiftmate.domain.openShift.entity.OpenShiftApply;
import com.example.shiftmate.domain.openShift.status.ApplyStatus;
import com.example.shiftmate.domain.storeMember.entity.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class OpenShiftApplyResDto {
    private Long id;
    private Long openShiftId;
    private String applicantName;
    private Department department;
    private ApplyStatus applyStatus;

    // 근무 정보 (목록 조회 시 사용)
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDateTime createdAt;

    public static OpenShiftApplyResDto from(OpenShiftApply apply) {
        return OpenShiftApplyResDto.builder()
                .id(apply.getId())
                .openShiftId(apply.getRequest().getId())
                .applicantName(apply.getApplicant().getUser().getName())
                .department(apply.getApplicant().getDepartment())
                .applyStatus(apply.getApplyStatus())
                .workDate(apply.getRequest().getWorkDate())
                .startTime(apply.getRequest().getShiftTemplate().getStartTime())
                .endTime(apply.getRequest().getShiftTemplate().getEndTime())
                .createdAt(apply.getCreatedAt())
                .build();
    }
}