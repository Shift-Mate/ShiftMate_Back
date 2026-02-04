package com.example.shiftmate.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity extends BaseCreateEntity {

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // soft delete 여부 확인
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // soft delete 실행
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // soft delete 복구
    public void restore() {
        this.deletedAt = null;
    }
}
