package com.example.shiftmate.domain.user.controller;

import com.example.shiftmate.domain.attendance.service.OtpService;
import com.example.shiftmate.domain.user.dto.request.UpdateMyProfileReqDto;
import com.example.shiftmate.domain.user.dto.response.*;
import com.example.shiftmate.domain.user.service.FileStorageService;
import com.example.shiftmate.domain.user.service.UserService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import com.example.shiftmate.domain.user.dto.response.UserDocumentResDto;
import com.example.shiftmate.domain.user.entity.UserDocumentType;
import com.example.shiftmate.domain.user.service.UserDocumentService;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.shiftmate.domain.user.dto.request.ChangePasswordReqDto;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.List;

// 사용자 관련 API 컨트롤러
@RestController
// final 필드 생성자 자동 생성
@RequiredArgsConstructor
// users 도메인 기본 경로
@RequestMapping("/users")
public class UserController {

    // 사용자 서비스 주입
    private final UserService userService;
    private final OtpService otpService;
    private final UserDocumentService userDocumentService;

    @GetMapping("/admin/user-info")
    public ApiResponse<UserInfoResDto> getUserInfoForManager(
        @RequestParam String email,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ApiResponse.success(userService.getUserInfoByEmailForManager(email, userDetails.getId()));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResDto> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(userService.getMyInfo(userDetails.getId()));
    }

    // [필터 API] 로그인 사용자가 근무한 기록이 있는 년/월 목록 조회
    @GetMapping("/me/salary/months")
    public ApiResponse<List<SalaryMonthResDto>> getMySalaryMonths(
            // 현재 로그인 사용자 정보(JWT) 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 사용자 기준 월 목록 조회
        List<SalaryMonthResDto> result = userService.getMySalaryMonths(userDetails.getId());

        // 공통 성공 응답으로 반환
        return ApiResponse.success(result);
    }

    // [집계 API] 선택한 년/월의 스토어별 월급 집계 조회
    @GetMapping("/me/salary/monthly")
    public ApiResponse<MonthlySalarySummaryResDto> getMyMonthlySalary(
            // 조회할 연도 (예: 2025)
            @RequestParam int year,
            // 조회할 월 (1~12)
            @RequestParam int month,
            // 현재 로그인 사용자 정보(JWT) 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 해당 년/월 집계 조회
        MonthlySalarySummaryResDto result =
                userService.getMyMonthlySalary(userDetails.getId(), year, month);

        // 공통 성공 응답으로 반환
        return ApiResponse.success(result);
    }

    // 로그인 사용자의 비밀번호 변경 API
    @PatchMapping("/me/password")
    public ApiResponse<String> changeMyPassword(
            // 요청 바디 유효성 검증
            @Valid @RequestBody ChangePasswordReqDto request,
            // 로그인 사용자 정보(JWT)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에 비밀번호 변경 위임
        userService.changeMyPassword(userDetails.getId(), request);

        // 성공 응답
        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }

    // 로그인 사용자의 프로필(이름/전화번호) 수정 API
    @PatchMapping("/me")
    public ApiResponse<UserInfoResDto> updateMyProfile(
            // 요청 바디 유효성 검증
            @Valid @RequestBody UpdateMyProfileReqDto request,
            // 로그인 사용자 정보
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 수정 처리 후 최신 사용자 정보 반환
        UserInfoResDto result = userService.updateMyProfile(userDetails.getId(), request);

        // 공통 성공 응답
        return ApiResponse.success(result);
    }

    // 출퇴근 처리용 OTP 번호 발급
    @PostMapping("/my/otp")
    public ResponseEntity<ApiResponse<String>> generateMyOtp(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String otp = otpService.generateAndSaveOtp(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(otp));
    }

    @PostMapping(value = "/me/documents/{type}", consumes = "multipart/form-data")
    public ApiResponse<UserDocumentResDto> uploadMyDocument(
            @PathVariable String type,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // path type 문자열을 enum으로 안전 변환
        UserDocumentType documentType = parseDocumentType(type);

        // 로그인 본인 문서 업로드
        UserDocumentResDto result =
                userDocumentService.uploadMyDocument(userDetails.getId(), documentType, file);

        return ApiResponse.success(result);
    }

    @GetMapping("/me/documents")
    public ApiResponse<List<UserDocumentResDto>> getMyDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인 본인 문서 목록 조회
        return ApiResponse.success(userDocumentService.getMyDocuments(userDetails.getId()));
    }

    @DeleteMapping("/me/documents/{type}")
    public ApiResponse<String> deleteMyDocument(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // path type 문자열을 enum으로 안전 변환
        UserDocumentType documentType = parseDocumentType(type);

        // 본인 문서 삭제
        userDocumentService.deleteMyDocument(userDetails.getId(), documentType);

        return ApiResponse.success("문서가 삭제되었습니다.");
    }

    @GetMapping("/me/documents/{type}/download")
    public ResponseEntity<Resource> downloadMyDocument(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) path 문자열을 enum으로 안전 변환한다.
        //    잘못된 타입 값이면 INVALID_REQUEST로 방어한다.
        UserDocumentType documentType = parseDocumentType(type);

        // 2) 서비스에서 본인 문서 스트림을 조회한다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMyDocument(userDetails.getId(), documentType);

        // 3) 다운로드 목적이므로 attachment 모드로 응답을 생성한다.
        return buildFileResponse(downloaded, true, false);
    }

    @GetMapping("/me/documents/{type}/preview")
    public ResponseEntity<Resource> previewMyDocument(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) 경로로 들어온 type 문자열을 enum으로 안전 변환한다.
        //    허용되지 않은 타입 값이면 즉시 INVALID_REQUEST로 차단한다.
        UserDocumentType documentType = parseDocumentType(type);

        // 2) 서비스에서 "본인 문서" 파일 스트림을 조회한다.
        //    여기서 문서 존재 여부/권한(본인 기준) 검증이 함께 처리된다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMyDocument(userDetails.getId(), documentType);

        // 3) 미리보기 목적이므로 inline 모드 + no-cache 응답을 생성한다.
        return buildFileResponse(downloaded, false, true);
    }

    private UserDocumentType parseDocumentType(String type) {
        // path 변수로 받은 문자열을 enum으로 변환한다.
        // 컨트롤러의 모든 문서 API에서 공통으로 재사용해 중복을 줄인다.
        try {
            return UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 지원하지 않는 문서 타입 요청은 일관되게 INVALID_REQUEST 처리한다.
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private ResponseEntity<Resource> buildFileResponse(
            FileStorageService.DownloadedFile downloaded,
            boolean asAttachment,
            boolean disableCache
    ) {
        // 저장소에서 받은 스트림을 HTTP 응답 바디 리소스로 감싼다.
        InputStreamResource resource = new InputStreamResource(downloaded.inputStream());

        // 파일명/타입/길이를 기반으로 다운로드 또는 미리보기 헤더를 구성한다.
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

        // preview 응답은 캐시 금지를 강제해 민감 문서가 브라우저에 남지 않게 한다.
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
