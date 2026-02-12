package com.example.shiftmate.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BiznoVerifyResDto {
    // 필수 항목
    private String bno;          // 사업자등록번호
    private String company;      // 회사명

    // 추가 정보 -> 필요시 사용
    private String bstt;         // 사업자상태(명칭) - 계속사업자(01), 휴업자(02), 폐업자(03)
    private String bSttCd;       // 사업자상태(코드) - 01, 02, 03
    private String cno;          // 법인등록번호
    private String taxTypeCd;    // 과세유형(코드)
    private String taxtype;      // 과세유형(명칭)
    private String endDt;        // 폐업일 (YYYYMMDD 형식)

}
