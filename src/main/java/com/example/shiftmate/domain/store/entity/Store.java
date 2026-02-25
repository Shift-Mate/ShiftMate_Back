package com.example.shiftmate.domain.store.entity;

import com.example.shiftmate.domain.shiftTemplate.entity.TemplateType;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column
    private TemplateType templateType;

    @Column(nullable = true)
    private Long monthlySales;

    @Column(nullable = true)
    private String imagePath;

    @Column(nullable = true)
    private String imageOriginalFileName;

    @Column(nullable = true)
    private String imageContentType;

    @Column(nullable = true)
    private Long imageSize;

    @Builder
    public Store(String name, String location, LocalTime openTime, LocalTime closeTime,
        Integer nShifts, String brn, User user, String alias, Long monthlySales) {
        this.name = name;
        this.location = location;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.nShifts = nShifts;
        this.brn = brn;
        this.user = user;
        this.alias = alias;
        this.templateType = null;
        this.monthlySales = monthlySales;
    }

    public void updateTemplateType(TemplateType templateType){
        this.templateType = templateType;
    }

    public void update(String name, String location, LocalTime openTime, LocalTime closeTime,
        Integer nShifts, String brn, User user, String alias, Long monthlySales) {
        if (name != null) this.name = name;
        if (location != null) this.location = location;
        if (openTime != null) this.openTime = openTime;
        if (closeTime != null) this.closeTime = closeTime;
        if (nShifts != null) this.nShifts = nShifts;
        if (brn != null) this.brn = brn;
        if (user != null) this.user = user;
        if (alias != null) this.alias = alias;
        if (monthlySales != null) this.monthlySales = monthlySales;
    }


    public boolean hasImage() {
        // 이미지 경로가 있으면 "가게 이미지가 등록되어 있다"로 간주한다.
        // 파일명/타입/사이즈는 부가 메타이고, 실체 파일 식별자는 imagePath다.
        return imagePath != null && !imagePath.isBlank();
    }

    public void replaceImage(String imagePath, String imageOriginalFileName, String imageContentType, Long imageSize) {
        // 업로드 성공 후 새 이미지 메타를 한 번에 교체한다.
        // 이 메서드는 "기존 이미지 덮어쓰기"와 "최초 등록" 둘 다 처리한다.
        this.imagePath = imagePath;
        this.imageOriginalFileName = imageOriginalFileName;
        this.imageContentType = imageContentType;
        this.imageSize = imageSize;
    }

    public void clearImage() {
        // 이미지 삭제 API 호출 시 DB 메타를 null로 비운다.
        // 실제 파일 삭제는 서비스 계층에서 먼저 수행한 뒤 이 메서드를 호출한다.
        this.imagePath = null;
        this.imageOriginalFileName = null;
        this.imageContentType = null;
        this.imageSize = null;
    }
}