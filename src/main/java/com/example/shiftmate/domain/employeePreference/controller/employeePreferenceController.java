package com.example.shiftmate.domain.employeePreference.controller;

import com.example.shiftmate.domain.employeePreference.dto.request.CreateWeeklyPreferenceReqDto;
import com.example.shiftmate.domain.employeePreference.dto.response.preferenceResDto;
import com.example.shiftmate.domain.employeePreference.service.PreferenceService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/stores/{storeId}/members/{memberId}")
@RestController
@RequiredArgsConstructor
public class employeePreferenceController {

    private final PreferenceService preferenceService;

    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse<Void>> createPreference(
        @PathVariable Long storeId,
        @PathVariable Long memberId,
        @RequestBody CreateWeeklyPreferenceReqDto preference
    ){
        System.out.println("test");
        preferenceService.createPreference(storeId,memberId,preference);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<List<preferenceResDto>>> getPreference(
        @PathVariable Long storeId,
        @PathVariable Long memberId
    ){

        return ResponseEntity.ok(ApiResponse.success(preferenceService.getPreference(storeId,memberId)));
    }

}
