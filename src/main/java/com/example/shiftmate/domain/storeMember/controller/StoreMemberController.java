package com.example.shiftmate.domain.storeMember.controller;

import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberUpdateReqDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberListResDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberResDto;
import com.example.shiftmate.domain.storeMember.dto.response.UserStoreListResDto;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.service.StoreMemberService;
import com.example.shiftmate.domain.user.dto.response.UserDocumentResDto;
import com.example.shiftmate.domain.user.entity.UserDocumentType;
import com.example.shiftmate.domain.user.service.FileStorageService;
import com.example.shiftmate.domain.user.service.UserDocumentService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{storeId}/store-members")
@RequiredArgsConstructor
public class StoreMemberController {

    private final StoreMemberService storeMemberService;
    private final UserDocumentService userDocumentService;

    // StoreMember create
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> createStoreMember(
        @PathVariable Long storeId,
        @PathVariable Long userId,
        @Valid @RequestBody StoreMemberReqDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeMemberService.createWithStoreId(
            storeId, userDetails.getId(), request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(null));
    }

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreMemberResDto>> getStoreMember(@PathVariable Long id) {
        StoreMemberResDto response = storeMemberService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 가게 기준 조회 (가게에 소속된 유저들) - 필터링 옵션: status, role, department
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreMemberListResDto>>> getMembersByStoreId(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) MemberStatus status,
        @RequestParam(required = false) StoreRole role,
        @RequestParam(required = false) Department department
    ) {
        List<StoreMemberListResDto> response = storeMemberService.getMembersByStoreId(
            storeId, userDetails.getId(), status, role, department);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreMemberResDto>> updateStoreMember(
        @PathVariable Long id,
        @Valid @RequestBody StoreMemberUpdateReqDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StoreMemberResDto response = storeMemberService.update(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStoreMember(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeMemberService.delete(id, userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(null));
    }

    @GetMapping("/{memberUserId}/documents")
    public ResponseEntity<ApiResponse<List<UserDocumentResDto>>> getMemberDocumentsForManager(
            @PathVariable Long storeId,
            @PathVariable Long memberUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) 로그인한 요청자(매니저 후보)의 userId를 기준으로 서비스 권한검사를 수행한다.
        // 2) 권한 통과 시 대상 멤버 문서 목록(보건증/신분증)을 반환한다.
        List<UserDocumentResDto> result = userDocumentService.getMemberDocumentsForManager(
                userDetails.getId(),
                storeId,
                memberUserId
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{memberUserId}/documents/{type}/download")
    public ResponseEntity<Resource> downloadMemberDocumentForManager(
            @PathVariable Long storeId,
            @PathVariable Long memberUserId,
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) path 문자열(type)을 enum으로 안전 변환한다.
        //    지원 타입 외 값이면 INVALID_REQUEST로 차단한다.
        final UserDocumentType documentType;
        try {
            documentType = UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 2) 서비스에서 권한검증 + 파일조회까지 처리한 다운로드 객체를 받는다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMemberDocumentForManager(
                        userDetails.getId(),
                        storeId,
                        memberUserId,
                        documentType
                );

        // 3) 응답 바디 리소스 생성
        //    (네 프로젝트에서 현재 쓰는 "내 문서 다운로드" 방식과 동일하게 맞춰서 사용)
        //    - ByteArrayResource 방식이면 bytes 기반
        //    - InputStreamResource 방식이면 inputStream 기반
        InputStreamResource resource = new InputStreamResource(downloaded.inputStream());

        // 4) 파일 다운로드 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloaded.contentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(downloaded.fileName(), StandardCharsets.UTF_8)
                        .build()
        );

        // 5) length가 제공되는 구현이면 헤더에 넣어준다.
        if (downloaded.contentLength() != null && downloaded.contentLength() >= 0) {
            headers.setContentLength(downloaded.contentLength());
        }

        // 6) 최종 파일 응답 반환
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
