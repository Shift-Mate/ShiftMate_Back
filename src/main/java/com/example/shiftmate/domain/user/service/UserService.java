package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.dto.response.MyStoreResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

// 서비스 계층임을 명시
@Service
// final 필드 생성자 자동 생성
@RequiredArgsConstructor
public class UserService {

    // store_members 조회용 레포지토리 주입
    private final StoreMemberRepository storeMemberRepository;

    // 로그인한 사용자의 소속 스토어 목록 조회
    public List<MyStoreResDto> getMyStores(Long userId) {
        // 1) userId로 store_members(내 소속) 조회
        List<StoreMember> members = storeMemberRepository.findByUserId(userId);

        // 2) 화면 응답 DTO로 변환 후 반환
        return members.stream()
                .map(member -> MyStoreResDto.builder()
                        // 스토어 PK
                        .storeId(member.getStore().getId())
                        // 스토어 이름
                        .storeName(member.getStore().getName())
                        // 스토어 별칭
                        .storeAlias(member.getStore().getAlias())
                        // 내 역할 enum -> 문자열
                        .role(member.getRole().name())
                        // DTO 생성 완료
                        .build())
                // List로 수집
                .toList();
    }
}