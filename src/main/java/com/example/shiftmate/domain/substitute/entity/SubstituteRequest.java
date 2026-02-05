package com.example.shiftmate.domain.substitute.entity;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "substitute_requests")
@NoArgsConstructor
public class SubstituteRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "substitute_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftassignment_id", nullable = false)
    private ShiftAssignment shiftAssignment; // 대타 요청 스케줄

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private StoreMember requester; // 대타 요청자 (해당 매장의 멤버만 신청 가능)

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // 대타 요청 상태

    private String reason; // 대타 요청 이유

    @Builder
    public SubstituteRequest(ShiftAssignment shiftAssignment, StoreMember requester, RequestStatus status, String reason) {
        this.shiftAssignment = shiftAssignment;
        this.requester = requester;
        this.status = status;
        this.reason = reason;
    }

    public void cancel() {
        this.status = RequestStatus.REQUESTER_CANCELED;
    }
}
