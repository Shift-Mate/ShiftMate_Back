package com.example.shiftmate.domain.store.service;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public StoreResDto create(StoreReqDto request, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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
            .build();

        // 저장
        Store savedStore = storeRepository.save(store);

        // DTO 변환
        return toResponseDto(savedStore);
    }

    // 단일 매장 조회, GET /stores/{storeId}
    public StoreResDto findById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return toResponseDto(store);
    }

    // 전체 매장 목록 조회(테스트용), GET /stores
    public List<StoreResDto> findAll() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
            .map(this::toResponseDto)
            .toList();
    }

    // todo 유저가 속해있는 매장 조회(StoreMember)

    // Store Update, PUT /stores/{storeId}
    @Transactional
    public StoreResDto update(Long storeId, StoreReqDto request, Long userId) {
        // store 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND)); // 매장 없음

        // 권한 확인
        if (!store.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);   // 권한 없음
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
            request.getAlias()
        );

        // dto 변환
        return toResponseDto(store);

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
            .build();
    }
}
