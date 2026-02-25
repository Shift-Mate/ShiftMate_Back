package com.example.shiftmate.domain.user.dto.response;

import com.example.shiftmate.domain.user.entity.UserDocument;
import com.example.shiftmate.domain.user.entity.UserDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDocumentResDto {

    // 문서 row 식별자
    private Long id;

    // 문서 타입 (HEALTH_CERTIFICATE / IDENTIFICATION)
    private UserDocumentType type;

    // 화면에서 보여줄 원본 파일명
    private String originalFileName;

    // 파일 MIME 타입
    private String contentType;

    // 파일 크기(byte)
    private Long size;

    // 접근 URL(로컬 테스트 단계에서는 local base url)
    private String fileUrl;

    public static UserDocumentResDto from(UserDocument doc) {
        // 엔티티를 API 응답용 DTO로 안전하게 변환한다.
        // 엔티티 전체를 노출하지 않고 필요한 값만 전달한다.
        return UserDocumentResDto.builder()
                .id(doc.getId())
                .type(doc.getType())
                .originalFileName(doc.getOriginalFileName())
                .contentType(doc.getContentType())
                .size(doc.getSize())
                .fileUrl(doc.getFileUrl())
                .build();
    }
}