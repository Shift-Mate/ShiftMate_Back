package com.example.shiftmate.domain.openShift.entity;

import com.example.shiftmate.domain.openShift.status.ApplyStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "open_shift_apply")
@Getter
@NoArgsConstructor
public class OpenShiftApply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private OpenShiftRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private StoreMember applicant;

    @Enumerated(EnumType.STRING)
    private ApplyStatus applyStatus;

    @Builder
    public OpenShiftApply(OpenShiftRequest request, StoreMember applicant, ApplyStatus applyStatus) {
        this.request = request;
        this.applicant = applicant;
        this.applyStatus = applyStatus;
    }

    public void changeApplyStatus(ApplyStatus applyStatus) {
        this.applyStatus = applyStatus;
    }
}
