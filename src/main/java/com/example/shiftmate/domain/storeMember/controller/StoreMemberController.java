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
        UserDocumentType documentType = parseDocumentType(type);

        // 2) 서비스에서 권한검증 + 파일조회까지 처리한 다운로드 객체를 받는다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMemberDocumentForManager(
                        userDetails.getId(),
                        storeId,
                        memberUserId,
                        documentType
                );

        // 3) 다운로드 목적이므로 attachment 모드 응답을 생성한다.
        return buildFileResponse(downloaded, true, false);
    }

    @GetMapping("/{memberUserId}/documents/{type}/preview")
    public ResponseEntity<Resource> previewMemberDocumentForManager(
            @PathVariable Long storeId,
            @PathVariable Long memberUserId,
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) 문서 타입 문자열을 enum으로 안전 변환한다.
        //    잘못된 타입은 INVALID_REQUEST로 처리한다.
        UserDocumentType documentType = parseDocumentType(type);

        // 2) 서비스에서 "매니저 권한 검증 + 대상 멤버 문서 조회"를 한 번에 수행한다.
        //    - 요청자가 해당 스토어 MANAGER인지
        //    - 대상 멤버가 같은 스토어 소속인지
        //    위 조건을 통과한 경우에만 파일 스트림을 반환한다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMemberDocumentForManager(
                        userDetails.getId(),
                        storeId,
                        memberUserId,
                        documentType
                );

        // 3) 미리보기 목적이므로 inline 모드 + no-cache 응답을 생성한다.
        return buildFileResponse(downloaded, false, true);
    }

    private UserDocumentType parseDocumentType(String type) {
        // path 변수로 받은 type 문자열을 enum으로 변환한다.
        // 지원하지 않는 값은 INVALID_REQUEST로 일관 처리한다.
        try {
            return UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private ResponseEntity<Resource> buildFileResponse(
            FileStorageService.DownloadedFile downloaded,
            boolean asAttachment,
            boolean disableCache
    ) {
        // 저장소에서 받은 스트림을 Resource 형태로 감싸서 응답 바디에 사용한다.
        InputStreamResource resource = new InputStreamResource(downloaded.inputStream());

        // 다운로드/미리보기 공통 헤더를 구성한다.
        // asAttachment=true 면 저장 다운로드, false 면 브라우저 inline 렌더링을 유도한다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloaded.contentType()));

        ContentDisposition disposition = asAttachment
                ? ContentDisposition.attachment()
                    .filename(downloaded.fileName(), StandardCharsets.UTF_8)
                    .build()
                : ContentDisposition.inline()
                    .filename(downloaded.fileName(), StandardCharsets.UTF_8)
                    .build();
        headers.setContentDisposition(disposition);

        if (downloaded.contentLength() != null && downloaded.contentLength() >= 0) {
            headers.setContentLength(downloaded.contentLength());
        }

        // 민감 문서 미리보기는 브라우저/프록시 캐시를 금지한다.
        if (disableCache) {
            headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
            headers.add("Pragma", "no-cache");
            headers.add("X-Content-Type-Options", "nosniff");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
