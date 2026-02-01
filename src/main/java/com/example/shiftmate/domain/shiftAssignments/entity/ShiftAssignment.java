package com.example.shiftmate.domain.shiftAssignments.entity;

import com.example.shiftmate.domain.shiftTemplates.entity.ShiftTemplate;
//import com.example.shiftmate.domain.store.entity.StoreMember;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shift_assignments")
public class ShiftAssignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO : 엔티티 생성시 주석 제거
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    private StoreMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private LocalDateTime updatedStartTime;

    @Column(nullable = false)
    private LocalDateTime updatedEndTime;

    @Builder
    public ShiftAssignment(ShiftTemplate shiftTemplate, LocalDate workDate, LocalDateTime updatedStartTime, LocalDateTime updatedEndTime) {
        this.shiftTemplate = shiftTemplate;
        this.workDate = workDate;
        this.updatedStartTime = updatedStartTime;
        this.updatedEndTime = updatedEndTime;
    }
    //TODO : 엔티티 생성시 해당 생성자 사용
//    @Builder
//    public ShiftAssignment(StoreMember member, ShiftTemplate shiftTemplate, LocalDate workDate, LocalDateTime updatedStartTime, LocalDateTime updatedEndTime) {
//        this.member = member;
//        this.shiftTemplate = shiftTemplate;
//        this.workDate = workDate;
//        this.updatedStartTime = updatedStartTime;
//        this.updatedEndTime = updatedEndTime;
//    }
}
