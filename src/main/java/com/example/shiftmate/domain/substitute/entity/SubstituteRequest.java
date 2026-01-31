package com.example.shiftmate.domain.substitute.entity;

import com.example.shiftmate.domain.schedule.entity.Schedule;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "substitute_requests")
@NoArgsConstructor
public class SubstituteRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subsitute_id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule; // 대타 요청 스케줄

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester; // 대타 요청자

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN; // 대타 요청 상태
    private String reason; // 대타 요청 이유
}
