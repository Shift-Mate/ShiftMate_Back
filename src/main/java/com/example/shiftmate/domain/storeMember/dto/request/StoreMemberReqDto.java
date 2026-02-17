package com.example.shiftmate.domain.storeMember.dto.request;

import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreMemberReqDto {

    @NotBlank(message = "추가할 사용자 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;


    @NotNull(message = "역할은 필수입니다.")
    private StoreRole role;

    // 2차
    private StoreRank memberRank;

    @NotNull(message = "부서는 필수입니다.")
    private Department department;

    // 2차
    private Integer hourlyWage;

    @NotNull(message = "주당 최소 근무 시간은 필수입니다.")
    private Integer minHoursPerWeek;

    private MemberStatus status;

    private String pinCode;

}
