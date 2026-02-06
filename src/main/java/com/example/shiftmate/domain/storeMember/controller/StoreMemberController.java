package com.example.shiftmate.domain.storeMember.controller;

import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberListResDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberResDto;
import com.example.shiftmate.domain.storeMember.dto.response.UserStoreListResDto;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.service.StoreMemberService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/store-members")
@RequiredArgsConstructor
public class StoreMemberController {

    private final StoreMemberService storeMemberService;

    // StoreMember create
    @PostMapping
    public ResponseEntity<ApiResponse<StoreMemberResDto>> createStoreMember(
        @Valid @RequestBody StoreMemberReqDto request
        // todo 임시: 인증(secu, jwt) 구현 후 수정 필요
        // @CurrentUserId Long userId) 으로 수정
    ) {
        StoreMemberResDto response = storeMemberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    // read
    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreMemberResDto>>> getAllStoreMembers() {
        List<StoreMemberResDto> response = storeMemberService.findAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreMemberResDto>> getStoreMember(@PathVariable Long id) {
        StoreMemberResDto response = storeMemberService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 유저 기준 조회 (유저가 소속된 가게 정보들)
    @GetMapping("/users/{userId}/stores")
    public ResponseEntity<ApiResponse<List<UserStoreListResDto>>> getStoresByUserId(
        @PathVariable Long userId
    ) {
        List<UserStoreListResDto> response = storeMemberService.getStoresByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 가게 기준 조회 (가게에 소속된 유저들) - 필터링 옵션: status, role, department
    @GetMapping("/stores/{storeId}/members")
    public ResponseEntity<ApiResponse<List<StoreMemberListResDto>>> getMembersByStoreId(
        @PathVariable Long storeId,
        @RequestParam(required = false) MemberStatus status,
        @RequestParam(required = false) StoreRole role,
        @RequestParam(required = false) Department department
    ) {
        List<StoreMemberListResDto> response = storeMemberService.getMembersByStoreId(
            storeId, status, role, department);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreMemberResDto>> updateStoreMember(
        @PathVariable Long id,
        @Valid @RequestBody StoreMemberReqDto request
    ) {
        StoreMemberResDto response = storeMemberService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStoreMember(@PathVariable Long id) {
        storeMemberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
