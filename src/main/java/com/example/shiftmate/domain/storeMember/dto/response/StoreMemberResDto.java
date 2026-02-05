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
public class StoreMemberResDto {
    private Long id;
    private Long storeId;
    private Long userId;
    private StoreRole role;
    private StoreRank memberRank;
    private Department department;
    private Integer hourlyWage;
    private Integer minHoursPerWeek;
    private MemberStatus status;
    private String pinCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
