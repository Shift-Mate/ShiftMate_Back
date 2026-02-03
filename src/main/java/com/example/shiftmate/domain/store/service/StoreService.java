package com.example.shiftmate.domain.store.service;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
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
