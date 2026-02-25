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
import org.springframework.core.io.ByteArrayResource;
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
        final UserDocumentType documentType;
        try {
            documentType = UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // HEALTH_CERTIFICATE / IDENTIFICATION 외 타입 방어
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

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
        final UserDocumentType documentType;
        try {
            documentType = UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 본인 문서 삭제
        userDocumentService.deleteMyDocument(userDetails.getId(), documentType);

        return ApiResponse.success("문서가 삭제되었습니다.");
    }

    @GetMapping("/me/documents/{type}/download")
    public ResponseEntity<ByteArrayResource> downloadMyDocument(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) path 문자열을 enum으로 안전 변환한다.
        //    잘못된 타입 값이면 INVALID_REQUEST로 방어한다.
        final UserDocumentType documentType;
        try {
            documentType = UserDocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 2) 서비스에서 본인 문서 바이트를 조회한다.
        FileStorageService.DownloadedFile downloaded =
                userDocumentService.downloadMyDocument(userDetails.getId(), documentType);

        // 3) 파일 응답 바디로 사용할 리소스를 만든다.
        ByteArrayResource resource = new ByteArrayResource(downloaded.bytes());

        // 4) 응답 헤더 구성
        //    - Content-Type: 브라우저/클라이언트가 파일 형식을 인식
        //    - Content-Disposition: attachment로 다운로드 유도
        //    - Content-Length: 다운로드 진행/검증에 활용
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloaded.contentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(downloaded.fileName(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.setContentLength(downloaded.bytes().length);

        // 5) 파일 스트림 응답 반환
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
