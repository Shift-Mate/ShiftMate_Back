package com.example.shiftmate.domain.shiftTemplate.entity;

import com.example.shiftmate.domain.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shift_templates")
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // 템플릿이 생성 된 후 인원을 입력하기때문에 null 을 허용
    @Column
    private Integer requiredStaff;


    // 해당 template가 어느 type에 속하는지 구분 ( 중첩-COSTSAVER , 분리-HIGHSERVICE )
    @Enumerated(EnumType.STRING)
    @Column
    private TemplateType templateType;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    @Column
    private DayType dayType;

    @Builder
    public ShiftTemplate(Store store, String name, LocalTime startTime, LocalTime endTime,
        Integer requiredStaff, ShiftType shiftType, DayType dayType, TemplateType templateType) {
        this.store = store;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredStaff = requiredStaff;
        this.shiftType = shiftType;
        this.dayType = dayType;
        this.templateType = templateType;
    }

    public void shiftStaff(Integer staffCount) {
        this.requiredStaff = staffCount;
    }
}
