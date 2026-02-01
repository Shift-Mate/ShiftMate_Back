package com.example.shiftmate.domain.store.entity;

import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "stores")
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String location;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false)
    private Integer nShifts;

    @Column(nullable = false)
    private String brn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true)
    private String alias;

    @Builder
    public Store(String name, String location, LocalTime openTime, LocalTime closeTime, Integer nShifts, String brn, User user, String alias) {
        this.name = name;
        this.location = location;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.nShifts = nShifts;
        this.brn = brn;
        this.user = user;
        this.alias = alias;
    }
}
