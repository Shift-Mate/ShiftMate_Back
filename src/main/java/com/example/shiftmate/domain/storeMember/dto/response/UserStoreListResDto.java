package com.example.shiftmate.domain.storeMember.dto.response;

import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 유저의 가게 목록 조회-> 유저가 소속된 가게
public class UserStoreListResDto {
    private Long storeMemberId;
    private Long storeId;
    private String storeName;
    private String storeLocation;
    private StoreRole role;
    private StoreRank memberRank;
    private Department department;
    private MemberStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}