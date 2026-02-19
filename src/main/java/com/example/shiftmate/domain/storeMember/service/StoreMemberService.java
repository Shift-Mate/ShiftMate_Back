package com.example.shiftmate.domain.storeMember.service;

import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberUpdateReqDto;
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

    // 매장에서 role이 MANAGER인 멤버만 멤버 추가 가능.
    @Transactional
    public void createWithStoreId(Long storeId, Long requestUserId, StoreMemberReqDto request,
        Long userId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 매장의 MANAGER만 멤버 추가 가능
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, requestUserId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        // 이메일로 사용자 조회 (해당하는 사람 확인)
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // body의 userId와 이메일로 조회한 user가 일치하는지 검증
        if (userId != null && !userId.equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 이미 해당 매장 멤버인지 중복 체크
        storeMemberRepository.findByStoreIdAndUserId(storeId, user.getId())
            .ifPresent(sm -> {
                throw new CustomException(ErrorCode.STORE_MEMBER_ALREADY_EXISTS);
            });

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

        StoreMember saved = storeMemberRepository.save(storeMember);
        toResponseDto(saved);
    }


    // 전체 조회
    public List<StoreMemberResDto> findAll() {
        List<StoreMember> storeMembers = storeMemberRepository.findAllWithRelations();
        return storeMembers.stream()
            .map(this::toResponseDto)
            .toList();
    }

    // 단일 조회-> StoreMemberId로 단일 조회
    // ex.000이 스타벅스 강남점에서 매니저로 일하고 있음, 시급 00, 주당 최소 근무시간 00
    public StoreMemberResDto findById(Long id) {
        StoreMember storeMember = storeMemberRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));
        return toResponseDto(storeMember);
    }

    @Transactional
    public StoreMemberResDto update(Long id, Long requestUserId, StoreMemberUpdateReqDto request) {
        // StoreMember 조회
        StoreMember storeMember = storeMemberRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));

        // 해당 매장의 MANAGER만 수정 가능
        Long storeId = storeMember.getStore().getId();
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, requestUserId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        // 업데이트 실행
        storeMember.update(
            request.getRole(),
            request.getMemberRank(),
            request.getDepartment(),
            request.getHourlyWage(),
            request.getMinHoursPerWeek(),
            request.getStatus(),
            request.getPinCode()
        );

        // DTO 변환
        return toResponseDto(storeMember);
    }

    @Transactional
    public void delete(Long id, Long requestUserId) {
        StoreMember storeMember = storeMemberRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));

        // 해당 매장의 MANAGER만 삭제 가능
        Long storeId = storeMember.getStore().getId();
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, requestUserId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        // soft delete 실행
        storeMember.delete();
    }

    // 유저 기준 조회 (유저가 소속된 가게 정보들)
    public List<UserStoreListResDto> getStoresByUserId(Long userId) {
        List<StoreMember> storeMembers = storeMemberRepository.findByUserId(userId);

        // 결과가 비어있으면 유저 존재 여부 확인 후 예외 또는 빈 리스트
        if (storeMembers.isEmpty()) {
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
        Long userId,
        MemberStatus status,
        StoreRole role,
        Department department
    ) {

        // 매장 소속 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Specification을 사용한 동적 쿼리
        Specification<StoreMember> spec = Specification
            .where(StoreMemberSpecification.hasStoreId(storeId))
            .and(StoreMemberSpecification.isNotDeleted())
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