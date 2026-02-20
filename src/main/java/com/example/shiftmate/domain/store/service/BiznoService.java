package com.example.shiftmate.domain.store.service;

import com.example.shiftmate.domain.store.client.BiznoApiClient;
import com.example.shiftmate.domain.store.dto.request.BiznoVerifyReqDto;
import com.example.shiftmate.domain.store.dto.response.BiznoVerifyResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BiznoService {

    private final BiznoApiClient biznoApiClient;

    public BiznoVerifyResDto verifyBusinessNumber(BiznoVerifyReqDto request) {
        log.info("사업자 번호 검증 요청: {}", request.getBno());

        BiznoVerifyResDto response = biznoApiClient.verifyBusinessNumber(request.getBno());

        log.info("사업자 번호 검증 결과: 회사명={}, 상태={}",
            response.getCompany(), response.getBstt());

        return response;
    }
}