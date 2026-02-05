package com.example.shiftmate.domain.storeMember.service;

import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberResDto;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreMemberService {
    private final StoreMemberRepository storeMemberRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public StoreMemberResDto create(StoreMemberReqDto request) {
        // 매장 조회
        Store store = storeRepository.findByIdAndDeletedAtIsNull(request.getStoreId())
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 중복 체크 (이미 해당 매장에 등록된 멤버인지 확인)
        storeMemberRepository.findByStoreIdAndUserId(request.getStoreId(), request.getUserId())
            .ifPresent(storeMember -> {
                throw new CustomException(ErrorCode.STORE_MEMBER_ALREADY_EXISTS);
            });

        // StoreMember 엔티티 생성
        StoreMember storeMember = StoreMember.builder()
            .store(store)
            .user(user)
            .role(request.getRole())
            .memberRank(request.getMemberRank())
            .department(request.getDepartment())
            .hourlyWage(request.getHourlyWage())
            .minHoursPerWeek(request.getMinHoursPerWeek())
            .status(request.getStatus())
            .pinCode(request.getPinCode())
            .build();

        // 저장
        StoreMember savedStoreMember = storeMemberRepository.save(storeMember);

        // DTO 변환
        return toResponseDto(savedStoreMember);
    }

    // StoreMember -> StoreMemberResDto 변환
    private StoreMemberResDto toResponseDto(StoreMember storeMember) {
        return StoreMemberResDto.builder()
            .id(storeMember.getId())
            .storeId(storeMember.getStore().getId())
            .userId(storeMember.getUser().getId())
            .role(storeMember.getRole())
            .memberRank(storeMember.getMemberRank())
            .department(storeMember.getDepartment())
            .hourlyWage(storeMember.getHourlyWage())
            .minHoursPerWeek(storeMember.getMinHoursPerWeek())
            .status(storeMember.getStatus())
            .pinCode(storeMember.getPinCode())
            .createdAt(storeMember.getCreatedAt())
            .updatedAt(storeMember.getUpdatedAt())
            .build();
    }
}