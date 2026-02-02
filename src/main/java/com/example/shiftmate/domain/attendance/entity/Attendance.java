package com.example.shiftmate.domain.attendance.entity;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "attendance")
@NoArgsConstructor
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ShiftAssignment shiftAssignment;

    private LocalDateTime clockInAt;
    private LocalDateTime clockOutAt;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Builder
    public Attendance(ShiftAssignment shiftAssignment, LocalDateTime clockInAt, LocalDateTime clockOutAt,  AttendanceStatus status) {
        this.shiftAssignment = shiftAssignment;
        this.clockInAt = clockInAt;
        this.clockOutAt = clockOutAt;
        this.status = status;
    }
}
