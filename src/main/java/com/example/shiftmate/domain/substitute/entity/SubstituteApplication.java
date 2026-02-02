package com.example.shiftmate.domain.substitute.entity;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "substitute_applications")
@NoArgsConstructor
public class SubstituteApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private SubstituteRequest request; // 어떤 대타 요청을 지원한건지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private StoreMember applicant; // 지원한 사람

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status; // 대타 요청 지원 상태

    @Builder
    public SubstituteApplication(SubstituteRequest request, StoreMember applicant, ApplicationStatus status) {
        this.request = request;
        this.applicant = applicant;
        this.status = status;
    }
}
