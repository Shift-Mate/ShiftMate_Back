package com.example.shiftmate.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

// 응답 DTO getter 자동 생성
@Getter
// 응답 DTO builder 패턴 사용
@Builder
public class MyStoreResDto {

    // 스토어 PK
    private Long storeId;

    // 스토어 이름
    private String storeName;

    // 스토어 별칭
    private String storeAlias;

    // 내 역할 문자열 (MANAGER / STAFF)
    private String role;
}
