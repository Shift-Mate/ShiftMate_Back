package com.example.shiftmate.domain.shiftTemplate.entity;

import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateShiftStaff;
import com.example.shiftmate.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

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
    public ShiftTemplate(Store store, String name, LocalTime startTime, LocalTime endTime, Integer requiredStaff, ShiftType shiftType, DayType dayType,TemplateType templateType) {
        this.store = store;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredStaff = requiredStaff;
        this.shiftType = shiftType;
        this.dayType = dayType;
        this.templateType = templateType;
    }

    public void shiftStaff(Integer staffCount){
        this.requiredStaff = staffCount;
    }
}
