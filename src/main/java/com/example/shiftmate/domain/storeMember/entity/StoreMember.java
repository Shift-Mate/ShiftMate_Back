package com.example.shiftmate.domain.storeMember.entity;

import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store_members")
public class StoreMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreRole role;

    @Enumerated(EnumType.STRING)
    @Column
    private StoreRank rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Column
    private Integer hourlyWage;

    @Column(nullable = false)
    private Integer minHoursPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private String pinCode;

    @Builder
    public StoreMember(Store store, User user, StoreRole role, StoreRank rank, Department department, Integer hourlyWage, Integer minHoursPerWeek, MemberStatus status, String pinCode) {
        this.store = store;
        this.user = user;
        this.role = role;
        this.rank = rank;
        this.department = department;
        this.hourlyWage = hourlyWage;
        this.minHoursPerWeek = minHoursPerWeek;
        this.status = status;
        this.pinCode = (pinCode != null) ? pinCode : "0000";
    }


}
