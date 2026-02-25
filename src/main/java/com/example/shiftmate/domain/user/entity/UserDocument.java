package com.example.shiftmate.domain.user.entity;

import com.example.shiftmate.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_documents_user_type",
                        columnNames = {"user_id", "type"}
                )
        }
)
public class UserDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자의 문서인지 소유자 관계를 저장한다.
    // 문서 조회/삭제 권한 검증의 기준이 된다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 문서 타입(보건증/신분증)을 구분한다.
    // 사용자당 타입 1개 정책을 유니크 제약으로 강제한다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserDocumentType type;

    // 업로드 당시 원본 파일명(화면 표시/관리 용도)
    @Column(nullable = false, length = 255)
    private String originalFileName;

    // MIME 타입(예: image/jpeg, application/pdf)
    // 검증 및 추후 다운로드 헤더 설정에 활용 가능
    @Column(nullable = false, length = 100)
    private String contentType;

    // 파일 크기(byte). 업로드 제한 검증과 표시용
    @Column(nullable = false)
    private Long size;

    // 로컬 저장소 실제 파일 경로
    // (S3 전환 시에는 s3 key로 바꿔도 됨)
    @Column(nullable = false, length = 1000)
    private String filePath;

    // 프론트 접근/미리보기용 URL
    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Builder
    public UserDocument(
            User user,
            UserDocumentType type,
            String originalFileName,
            String contentType,
            Long size,
            String filePath,
            String fileUrl
    ) {
        // 최초 업로드 시 문서 메타데이터를 구성한다.
        this.user = user;
        this.type = type;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = size;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
    }

    public void replaceFile(
            String originalFileName,
            String contentType,
            Long size,
            String filePath,
            String fileUrl
    ) {
        // 재업로드 시 기존 row를 재사용하고 파일 정보만 교체한다.
        // 타입당 1개 정책을 유지하면서 최신 파일만 남길 수 있다.
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = size;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
    }
}