package com.example.shiftmate.domain.store.controller;

import com.example.shiftmate.domain.store.dto.request.BiznoVerifyReqDto;
import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.BiznoVerifyResDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.service.StoreService;
import com.example.shiftmate.domain.store.service.BiznoService;
import com.example.shiftmate.domain.user.service.FileStorageService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final BiznoService biznoService;

    // store create (사용자가 매장 생성자로 등록됨)
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResDto>> createStore(
            @Valid @RequestBody StoreReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StoreResDto response = storeService.create(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 단일 매장 조회, GET /stores/{storeId}
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> getStore(@PathVariable Long storeId){
        StoreResDto response = storeService.findById(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그인한 유저가 속한 매장 목록 조회, GET /stores
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResDto>>> getAllStores(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<StoreResDto> response = storeService.findStoresByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 유저가 속해있는 매장 수정(해당 매장 MANAGER만 수정 가능)
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> updateStore(
        @PathVariable Long storeId,
        @Valid @RequestBody StoreReqDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        StoreResDto response = storeService.update(storeId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Store Delete, DELETE /stores/{storeId}
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        storeService.delete(storeId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }

    // 사업자 번호 검증, POST /stores/verify-bizno
    @PostMapping("/verify-bizno")
    public ResponseEntity<ApiResponse<BiznoVerifyResDto>> verifyBusinessNumber(
        @Valid @RequestBody BiznoVerifyReqDto request
    ) {
        BiznoVerifyResDto response = biznoService.verifyBusinessNumber(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/{storeId}/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<StoreResDto>> uploadStoreImage(
            @PathVariable Long storeId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) 업로드 요청을 서비스 계층으로 위임한다.
        //    컨트롤러는 HTTP 요청/응답 변환에 집중하고,
        //    실제 비즈니스 규칙(매니저 권한, 이미지 MIME/용량 검증, 저장소 업로드)은 서비스가 담당한다.
        StoreResDto response = storeService.uploadStoreImage(storeId, userDetails.getId(), file);

        // 2) 업로드 직후의 최신 Store DTO를 반환한다.
        //    프론트는 응답값을 받아 대시보드 카드 이미지를 즉시 갱신할 수 있다.
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storeId}/image/preview")
    public ResponseEntity<Resource> previewStoreImage(@PathVariable Long storeId) {
        // 1) 서비스에서 이미지 스트림과 메타정보(파일명/타입/길이)를 가져온다.
        //    storage-type(local/s3)에 따른 파일 읽기 차이는 FileStorageService 구현체가 숨긴다.
        FileStorageService.DownloadedFile downloaded = storeService.previewStoreImage(storeId);

        // 2) 스트림을 Resource로 감싸 ResponseEntity 본문으로 내려줄 준비를 한다.
        //    InputStreamResource를 사용하면 파일 전체를 메모리에 올리지 않고 스트리밍할 수 있다.
        InputStreamResource resource = new InputStreamResource(downloaded.inputStream());

        // 3) 브라우저 렌더링에 필요한 응답 헤더를 구성한다.
        //    - Content-Type: 브라우저가 이미지 MIME을 정확히 인식하도록 지정
        //    - Content-Disposition: inline으로 설정해 "다운로드"가 아닌 "미리보기" 동작 유도
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloaded.contentType()));
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(downloaded.fileName(), StandardCharsets.UTF_8)
                        .build()
        );

        // 4) 길이 정보를 알고 있으면 Content-Length를 추가한다.
        //    일부 브라우저/프록시가 응답 처리를 더 안정적으로 수행할 수 있다.
        if (downloaded.contentLength() != null && downloaded.contentLength() >= 0) {
            headers.setContentLength(downloaded.contentLength());
        }

        // 5) 이미지 교체 직후에도 최신 파일이 보이도록 캐시를 억제한다.
        //    운영에서 CDN 캐시를 쓰고 싶다면 정책에 맞게 이 부분을 조정하면 된다.
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        // 6) 최종 미리보기 응답을 반환한다.
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @DeleteMapping("/{storeId}/image")
    public ResponseEntity<ApiResponse<Void>> deleteStoreImage(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1) 삭제 처리를 서비스 계층에 위임한다.
        //    서비스에서 권한 확인(매니저), 저장소 파일 삭제, DB 메타데이터 정리를 순서대로 수행한다.
        storeService.deleteStoreImage(storeId, userDetails.getId());

        // 2) 프로젝트 공통 응답 포맷으로 성공 결과를 반환한다.
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
