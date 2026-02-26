package com.example.shiftmate.domain.openShift.entity;

import com.example.shiftmate.domain.openShift.status.RequestStatus;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "open_shift_request")
@Getter
@NoArgsConstructor
public class OpenShiftRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private LocalDate workDate;

    // 해당 오픈시프트에 대한 참고사항
    private String note;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Builder
    public OpenShiftRequest(Store store, ShiftTemplate shiftTemplate, LocalDate workDate, String note, RequestStatus requestStatus) {
        this.store = store;
        this.shiftTemplate = shiftTemplate;
        this.workDate = workDate;
        this.note = note;
        this.requestStatus = requestStatus;
    }

    public void changeRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
