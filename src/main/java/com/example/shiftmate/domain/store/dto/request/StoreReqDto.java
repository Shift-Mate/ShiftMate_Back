package com.example.shiftmate.domain.store.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class StoreReqDto {

    @NotBlank(message = "매장명은 필수입니다.")
    private String name;

    // 2차 고려
    private String location;

    @NotNull(message = "영업 시작 시간은 필수입니다.")
    private LocalTime openTime;

    @NotNull(message = "영업 종료 시간은 필수입니다.")
    private LocalTime closeTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @NotNull(message = "교대 설정값은 필수입니다.")
    private Integer nShifts;

    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Pattern(regexp = "\\d{3}-\\d{2}-\\d{5}", message = "사업자번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String brn;

    private String alias;
}
