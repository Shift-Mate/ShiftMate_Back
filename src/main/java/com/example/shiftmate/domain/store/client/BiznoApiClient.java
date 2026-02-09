package com.example.shiftmate.domain.store.client;

import com.example.shiftmate.domain.store.dto.response.BiznoVerifyResDto;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiznoApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${bizno.api.url}")
    private String apiUrl;

    @Value("${bizno.api.key}")
    private String apiKey;

    public BiznoVerifyResDto verifyBusinessNumber(String bno) {
        try {
            // 하이픈 제거
            String cleanBno = bno.replace("-", "");

            // API 호출 URL 구성
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(apiUrl)
                .queryParam("key", apiKey)
                .queryParam("gb", "1")
                .queryParam("q", cleanBno)
                .queryParam("type", "json");

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
            );

            // 응답 파싱
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return parseResponse(jsonNode, bno);

        } catch (Exception e) {
            log.error("비즈노 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.BIZNO_API_ERROR);
        }
    }

    private BiznoVerifyResDto parseResponse(JsonNode jsonNode, String bno) {
        // 실제 비즈노 API 응답 구조: { "resultCode": 0, "items": [...] }
        JsonNode items = jsonNode.get("items");

        if (items == null || !items.isArray() || items.size() == 0) {
            log.warn("API 응답에 items 필드가 없거나 비어있습니다. 응답: {}", jsonNode.toString());
            throw new CustomException(ErrorCode.INVALID_BIZNO);
        }

        // items 배열의 첫 번째 요소 가져오기 (null이 아닌 첫 번째 항목)
        JsonNode item = null;
        for (int i = 0; i < items.size(); i++) {
            JsonNode currentItem = items.get(i);
            if (currentItem != null && !currentItem.isNull()) {
                item = currentItem;
                break;
            }
        }

        if (item == null) {
            log.warn("사업자 번호에 대한 유효한 데이터가 없습니다: {}", bno);
            throw new CustomException(ErrorCode.INVALID_BIZNO);
        }

        // 필드 추출 (null 체크 포함)
        String responseBno = getTextValue(item, "bno", bno);
        String company = getTextValue(item, "company", null);
        String bstt = getTextValue(item, "bstt", null);
        String BSttCd = getTextValue(item, "bsttcd", null);
        String cno = getTextValue(item, "cno", null);
        String TaxTypeCd = getTextValue(item, "TaxTypeCd", null);
        String taxtype = getTextValue(item, "taxtype", null);
        String EndDt = getTextValue(item, "EndDt", null);

        return BiznoVerifyResDto.builder()
            .bno(responseBno)
            .company(company)
            .bstt(bstt)
            .BSttCd(BSttCd)
            .cno(cno)
            .TaxTypeCd(TaxTypeCd)
            .taxtype(taxtype)
            .EndDt(EndDt)
            .build();
    }

    private String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }
}