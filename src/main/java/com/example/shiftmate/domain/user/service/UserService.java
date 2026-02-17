package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.repository.AttendanceRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.dto.response.MyStoreProfileResDto;
import com.example.shiftmate.domain.user.dto.response.MyStoreResDto;
import com.example.shiftmate.domain.user.dto.response.UserInfoResDto;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.shiftmate.global.exception.ErrorCode;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

// 서비스 계층임을 명시
@Service
// final 필드 생성자 자동 생성
@RequiredArgsConstructor
public class UserService {

    // store_members 조회용 레포지토리 주입
    private final StoreMemberRepository storeMemberRepository;
    private final AttendanceRepository attendanceRepository; // 출근기록 조회 레포지토리
    private final UserRepository userRepository;

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

    // 로그인한 사용자가 선택한 스토어에서의 내 상세 정보 조회
    public MyStoreProfileResDto getMyStoreProfile(Long userId, Long storeId, LocalDate baseDate) { // 상세 조회 메서드 시작
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId) // 스토어+유저로 내 소속 검증 조회
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND)); // 소속 없으면 예외 처리

        LocalDate targetDate = (baseDate != null) ? baseDate : LocalDate.now(); // 기준일 없으면 오늘 사용
        LocalDate weekStart = targetDate.with(DayOfWeek.MONDAY); // 기준일이 속한 주의 월요일 계산
        LocalDate weekEnd = targetDate.with(DayOfWeek.SUNDAY); // 기준일이 속한 주의 일요일 계산

        List<Attendance> attendances = attendanceRepository.findAllByMemberIdAndWorkDateBetween( // 해당 주 근무기록 조회
                member.getId(), // 내 store_member id 전달
                weekStart, // 조회 시작일 전달
                weekEnd // 조회 종료일 전달
        ); // 조회 호출 끝

        long weeklyWorkedMinutes = attendances.stream() // 스트림 시작
                .mapToLong(attendance -> Duration.between( // 각 기록의 근무시간 계산
                        attendance.getClockInAt(), // 출근 시각
                        attendance.getClockOutAt() // 퇴근 시각
                ).toMinutes()) // 분 단위로 변환
                .sum(); // 전체 합계

        return MyStoreProfileResDto.builder() // 상세 DTO 빌더 시작
                .storeId(member.getStore().getId()) // 스토어 ID
                .storeName(member.getStore().getName()) // 스토어 이름
                .storeAlias(member.getStore().getAlias()) // 스토어 별칭
                .role(member.getRole().name()) // 내 역할
                .department(member.getDepartment().name()) // 내 부서
                .hourlyWage(member.getHourlyWage()) // 시급
                .minHoursPerWeek(member.getMinHoursPerWeek()) // 주간 최소근무시간
                .status(member.getStatus().name()) // 멤버 상태
                .weeklyWorkedMinutes(weeklyWorkedMinutes) // 이번 주 실제 근무 분 합계
                .build(); // DTO 빌더 종료
    }

    public UserInfoResDto getUserInfoByEmailForManager(String email, Long userId) {
        // 접근 사용자가 소속 매장 중 하나에서 MANAGER 권한을 갖는지 검증
        boolean isManager = storeMemberRepository.findByUserId(userId).stream()
            .anyMatch(member -> member.getRole() == StoreRole.MANAGER);
        if (!isManager) {
            throw new CustomException(ErrorCode.STORE_MEMBER_ACCESS_DENIED);
        }

        User user = userRepository.findByEmail(email).orElseThrow(
            ()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        return UserInfoResDto.from(user);
    }

    public UserInfoResDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        return UserInfoResDto.from(user);
    }
}
