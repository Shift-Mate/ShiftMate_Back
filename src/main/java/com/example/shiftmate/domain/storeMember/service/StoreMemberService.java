package com.example.shiftmate.domain.storeMember.service;

import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberListResDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberResDto;
import com.example.shiftmate.domain.storeMember.dto.response.UserStoreListResDto;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberSpecification;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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

    // 전체 조회
    public List<StoreMemberResDto> findAll() {
        List<StoreMember> storeMembers = storeMemberRepository.findAllWithRelations();
        return storeMembers.stream()
            .map(this::toResponseDto)
            .toList();
    }

    // 단일 조회-> StorememberId로 단일 조회
    // ex.000이 스타벅스 강남점에서 매니저로 일하고 있음, 시급 00, 주당 최소 근무시간 00
    public StoreMemberResDto findById(Long id) {
        StoreMember storeMember = storeMemberRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));
        return toResponseDto(storeMember);
    }

    // 유저 기준 조회 (유저가 소속된 가게 정보들)
    public List<UserStoreListResDto> getStoresByUserId(Long userId) {
        List<StoreMember> storeMembers = storeMemberRepository.findByUserId(userId);

        // 결과가 비어있으면 유저가 없거나 가게가 없는 경우
        if (storeMembers.isEmpty()) {
            // 유저 존재 여부 확인 (선택적)
            userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        }

        return storeMembers.stream()
            .map(this::toUserStoreListDto)
            .toList();
    }

    // 가게 기준 조회 (가게에 소속된 유저들) - 필터링 옵션(status, role, department)
    public List<StoreMemberListResDto> getMembersByStoreId(
        Long storeId,
        MemberStatus status,
        StoreRole role,
        Department department
    ) {
        // Specification을 사용한 동적 쿼리
        Specification<StoreMember> spec = Specification
            .where(StoreMemberSpecification.hasStoreId(storeId))
            .and(StoreMemberSpecification.hasStatus(status))
            .and(StoreMemberSpecification.hasRole(role))
            .and(StoreMemberSpecification.hasDepartment(department));

        List<StoreMember> storeMembers = storeMemberRepository.findAll(spec);

        // 결과가 비어있으면 가게가 없거나 멤버가 없는 경우
        if (storeMembers.isEmpty()) {
            storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        }

        return storeMembers.stream()
            .map(this::toStoreMemberListDto)
            .toList();
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

    // StoreMember -> UserStoreListResDto 변환
    private UserStoreListResDto toUserStoreListDto(StoreMember storeMember) {
        return UserStoreListResDto.builder()
            .storeMemberId(storeMember.getId())
            .storeId(storeMember.getStore().getId())
            .storeName(storeMember.getStore().getName())
            .storeLocation(storeMember.getStore().getLocation())
            .role(storeMember.getRole())
            .memberRank(storeMember.getMemberRank())
            .department(storeMember.getDepartment())
            .status(storeMember.getStatus())
            .createdAt(storeMember.getCreatedAt())
            .updatedAt(storeMember.getUpdatedAt())
            .build();
    }

    // StoreMember -> StoreMemberListResDto 변환
    private StoreMemberListResDto toStoreMemberListDto(StoreMember storeMember) {
        return StoreMemberListResDto.builder()
            .id(storeMember.getId())
            .userId(storeMember.getUser().getId())
            .userName(storeMember.getUser().getName())
            .userEmail(storeMember.getUser().getEmail())
            .role(storeMember.getRole())
            .memberRank(storeMember.getMemberRank())
            .department(storeMember.getDepartment())
            .hourlyWage(storeMember.getHourlyWage())
            .minHoursPerWeek(storeMember.getMinHoursPerWeek())
            .status(storeMember.getStatus())
            .createdAt(storeMember.getCreatedAt())
            .updatedAt(storeMember.getUpdatedAt())
            .build();
    }
}