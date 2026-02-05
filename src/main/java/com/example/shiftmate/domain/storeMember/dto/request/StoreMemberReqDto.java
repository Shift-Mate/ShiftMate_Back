package com.example.shiftmate.domain.storeMember.dto.request;

import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreMemberReqDto {

    @NotNull(message = "매장 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "역할은 필수입니다.")
    private StoreRole role;

    //2차
    private StoreRank memberRank;

    @NotNull(message = "부서는 필수입니다.")
    private Department department;

    // 2차
    private Integer hourlyWage;

    @NotNull(message = "주당 최소 근무 시간은 필수입니다.")
    private Integer minHoursPerWeek;

    @NotNull(message = "멤버 상태는 필수입니다.")
    private MemberStatus status;

    private String pinCode;

}
