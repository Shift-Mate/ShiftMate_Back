package com.example.shiftmate.domain.employeePreference.entity;

import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "employee_preferences")
public class EmployeePreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private StoreMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private Integer dayOfSeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferenceType type;

    @Builder
    public EmployeePreference(StoreMember member, ShiftTemplate shiftTemplate, Integer dayOfSeek, PreferenceType type) {
        this.member = member;
        this.shiftTemplate = shiftTemplate;
        this.dayOfSeek = dayOfSeek;
        this.type = type;
    }

}
