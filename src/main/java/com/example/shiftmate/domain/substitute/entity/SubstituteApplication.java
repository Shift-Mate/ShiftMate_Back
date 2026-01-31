package com.example.shiftmate.domain.substitute.entity;

import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "substitute_applications")
@NoArgsConstructor
public class SubstituteApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long application_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private SubsituteRequest request; // 어떤 대타 요청을 지원한건지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant; // 지원한 사람

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.WAITING; // 대타 요청 지원 상태
}
