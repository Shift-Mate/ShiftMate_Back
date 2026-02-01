package com.example.shiftmate.domain.shiftTemplates.entity;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer requiredStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayType dayType;

    @Builder
    public ShiftTemplate(Store store, String name, LocalTime startTime, LocalTime endTime, Integer requiredStaff, ShiftType shiftType, DayType dayType) {
        this.store = store;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredStaff = requiredStaff;
        this.shiftType = shiftType;
        this.dayType = dayType;
    }
}
