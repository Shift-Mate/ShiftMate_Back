package com.example.shiftmate.domain.storeMember.dto.request;

import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class StoreMemberUpdateReqDto {

    @NotNull(message = "역할은 필수입니다.")
    private StoreRole role;

    private StoreRank memberRank;

    @NotNull(message = "부서는 필수입니다.")
    private Department department;

    @Min(value = 0, message = "시급은 0 이상이어야 합니다.")
    private Integer hourlyWage;

    @NotNull(message = "주당 최소 근무 시간은 필수입니다.")
    @Min(value = 0, message = "주당 최소 근무 시간은 0 이상이어야 합니다.")
    private Integer minHoursPerWeek;

    @NotNull(message = "멤버 상태는 필수입니다.")
    private MemberStatus status;

    @Size(min = 4, max = 6, message = "PIN 코드는 4~6자리여야 합니다.")
    @Pattern(regexp = "^[0-9]*$", message = "PIN 코드는 숫자만 입력 가능합니다.")
    private String pinCode;
}
