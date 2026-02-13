package com.example.shiftmate.domain.store.service;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreMemberRepository storeMemberRepository;

    @Transactional
    public StoreResDto create(StoreReqDto request, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 영업 시작 시간은 종료 시간보다 빨라야 함
        if (!request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Store 엔티티 생성
        Store store = Store.builder()
            .name(request.getName())
            .location(request.getLocation())
            .openTime(request.getOpenTime())
            .closeTime(request.getCloseTime())
            .nShifts(request.getNShifts())
            .brn(request.getBrn())
            .user(user)
            .alias(request.getAlias())
            .monthlySales(request.getMonthlySales())
            .build();

        // 저장
        Store savedStore = storeRepository.save(store);

        // 생성자를 매장 멤버로 자동 등록 (MANAGER)
        StoreMember creatorMember = StoreMember.builder()
            .store(savedStore)
            .user(user)
            .role(StoreRole.MANAGER)
            .memberRank(StoreRank.MANAGER)
            .department(Department.HALL)
            .hourlyWage(null)
            .minHoursPerWeek(0)
            .status(MemberStatus.ACTIVE)
            .pinCode(null)
            .build();
        storeMemberRepository.save(creatorMember);

        // DTO 변환
        return toResponseDto(savedStore);
    }

    // 단일 매장 조회, GET /stores/{storeId}
    public StoreResDto findById(Long storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return toResponseDto(store);
    }

    // 로그인한 유저가 속한 매장 목록 조회, GET /stores
    public List<StoreResDto> findStoresByUserId(Long userId) {
        List<StoreMember> storeMembers = storeMemberRepository.findByUserId(userId);

        if (storeMembers.isEmpty()) {
            return List.of();
        }

        return storeMembers.stream()
                .map(StoreMember::getStore)
                .filter(store -> !store.isDeleted())
                .collect(Collectors.toMap(Store::getId, store -> store, (a, b) -> a))
                .values()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }
    // Store Update, PUT /stores/{storeId}
    @Transactional
    public StoreResDto update(Long storeId, StoreReqDto request, Long userId) {
        // store 조회 (삭제되지 않은 + 삭제 권한 확인)
        Store store = storeRepository.findByIdAndUserIdAndDeletedAtIsNull(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND)); // 매장 or 권한 없음

        // 영업 시작 시간은 종료 시간보다 빨라야 함 (변경된 경우만 검사하려면 request 값 기준)
        if (request.getOpenTime() != null && request.getCloseTime() != null
                && !request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }
        
        // store update
        store.update(
            request.getName(),
            request.getLocation(),
            request.getOpenTime(),
            request.getCloseTime(),
            request.getNShifts(),
            request.getBrn(),
            null, // user는 업데이트x
            request.getAlias(),
            request.getMonthlySales()
        );

        // dto 변환
        return toResponseDto(store);

    }

    // Store Delete(Soft Delete)
    // DELETE /stores/{storeId}
    @Transactional
    public void delete(Long storeId, Long userId) {
        // store 조회 (삭제되지 않은 + 삭제 권한 확인)
        Store store = storeRepository.findByIdAndUserIdAndDeletedAtIsNull(storeId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND)); // 매장 없음 or 권한 없음

        // Soft Delete 실행
        store.softDelete();
    }

    // Store -> StoreResDto 변환
    private StoreResDto toResponseDto(Store store) {
        return StoreResDto.builder()
            .id(store.getId())
            .name(store.getName())
            .alias(store.getAlias())
            .openTime(store.getOpenTime())
            .closeTime(store.getCloseTime())
            .createdAt(store.getCreatedAt())
            .updatedAt(store.getUpdatedAt())
            .monthlySales(store.getMonthlySales())
            .build();
    }
}
