package com.example.shiftmate.domain.openShift.dto.response;

import com.example.shiftmate.domain.openShift.entity.OpenShiftApply;
import com.example.shiftmate.domain.openShift.status.ApplyStatus;
import com.example.shiftmate.domain.storeMember.entity.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OpenShiftApplyResDto {
    private Long id;
    private String applicantName;
    private Department department;
    private ApplyStatus applyStatus;
    private LocalDateTime createdAt;

    public static OpenShiftApplyResDto from(OpenShiftApply apply) {
        return OpenShiftApplyResDto.builder()
                .id(apply.getId())
                .applicantName(apply.getApplicant().getUser().getName())
                .department(apply.getApplicant().getDepartment())
                .applyStatus(apply.getApplyStatus())
                .createdAt(apply.getCreatedAt())
                .build();
    }
}
