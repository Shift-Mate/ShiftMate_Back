package com.example.shiftmate.domain.user.service;


import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.entity.AttendanceStatus;
import com.example.shiftmate.domain.attendance.repository.AttendanceRepository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;

import com.example.shiftmate.domain.user.dto.request.UpdateMyProfileReqDto;
import com.example.shiftmate.domain.user.dto.response.MonthlySalarySummaryResDto;
import com.example.shiftmate.domain.user.dto.response.SalaryMonthResDto;
import com.example.shiftmate.domain.user.dto.response.StoreMonthlySalaryResDto;
import com.example.shiftmate.domain.user.dto.response.UserInfoResDto;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.shiftmate.global.exception.ErrorCode;

import com.example.shiftmate.domain.user.dto.request.ChangePasswordReqDto;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// 서비스 계층임을 명시
@Service
// final 필드 생성자 자동 생성
@RequiredArgsConstructor
public class UserService {

    // store_members 조회용 레포지토리 주입
    private final StoreMemberRepository storeMemberRepository;
    private final AttendanceRepository attendanceRepository; // 출근기록 조회 레포지토리
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 급여 계산용 시각 보정 규칙
    // 00~14분 -> :00, 15~44분 -> :30, 45~59분 -> 다음 시각 :00
    private LocalDateTime normalizePayrollTime(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        LocalDateTime base = dateTime.withSecond(0).withNano(0);

        if (minute < 15) {
            return base.withMinute(0);
        }
        if (minute < 45) {
            return base.withMinute(30);
        }
        return base.plusHours(1).withMinute(0);
    }

    // 스토어별 월 집계 중간 계산용 내부 클래스
    private static class StoreMonthlyAccumulator {
        // 매장 ID (그룹핑 키)
        private final Long storeId;
        // 매장 이름 (응답 표시용)
        private final String storeName;
        // 매장 별칭 (없을 수 있음)
        private final String storeAlias;

        // 대표 시급 (월 중 변동이 있으면 마지막 반영값으로 덮어씀)
        private int representativeHourlyWage;
        // 월 누적 근무 분(minute)
        private long workedMinutes;
        // 월 누적 예상 급여(원)
        private long estimatedPay;

        // 누적기 생성자: 매장 기본 정보 + 초기 시급 세팅
        private StoreMonthlyAccumulator(Long storeId, String storeName, String storeAlias, int representativeHourlyWage) {
            // 매장 ID 저장
            this.storeId = storeId;
            // 매장 이름 저장
            this.storeName = storeName;
            // 매장 별칭 저장
            this.storeAlias = storeAlias;
            // 대표 시급 초기값 저장
            this.representativeHourlyWage = representativeHourlyWage;
        }

        // 근무 1건을 누적하는 메서드
        private void addWork(long minutes, int hourlyWage) {
            // 음수 분 데이터 방지: 0 미만이면 0으로 보정 후 근무 분 누적
            this.workedMinutes += Math.max(0, minutes);
            // (분 * 시급 / 60)으로 급여 계산 후 반올림하여 누적
            this.estimatedPay += Math.round(Math.max(0, minutes) * hourlyWage / 60.0);
            // 이번 건의 시급을 대표 시급으로 갱신
            this.representativeHourlyWage = hourlyWage;
        }
    }

    // [필터용] 로그인 사용자의 "근무 기록이 있는" 년/월 목록을 반환하는 메서드
    public List<SalaryMonthResDto> getMySalaryMonths(Long userId) {
        // 출근/퇴근이 모두 찍힌 완료 근무 기록만 조회
        List<Attendance> attendances = attendanceRepository.findCompletedAttendancesByUserId(userId);

        // Attendance 목록을 스트림으로 순회 시작
        return attendances.stream()
                // 각 근무 기록에서 workDate를 꺼내 YearMonth(연-월)로 변환
                .map(a -> YearMonth.from(a.getShiftAssignment().getWorkDate()))
                // 같은 연-월 중복 제거
                .distinct()
                // 최신 월이 먼저 오도록 내림차순 정렬
                .sorted(Comparator.reverseOrder())
                // 응답 DTO 형태(year, month)로 매핑
                .map(ym -> new SalaryMonthResDto(ym.getYear(), ym.getMonthValue()))
                // List로 수집해서 반환
                .collect(Collectors.toList());
    }

    // [월별 집계용] 특정 연/월 기준으로 스토어별 시급/근무시간/예상급여를 반환하는 메서드
    public MonthlySalarySummaryResDto getMyMonthlySalary(Long userId, int year, int month) {
        // month 파라미터가 1~12 범위인지 1차 검증
        if (month < 1 || month > 12) {
            // 잘못된 요청이면 공통 INVALID_REQUEST 예외 발생
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 검증 완료된 YearMonth를 담을 변수 선언
        final YearMonth yearMonth;
        try {
            // year/month로 YearMonth 객체 생성 (연도 비정상 값도 여기서 검증됨)
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            // 날짜 생성 실패 시 잘못된 요청 예외로 변환
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 해당 월의 시작일(예: 2025-11-01) 계산
        LocalDate startDate = yearMonth.atDay(1);
        // 해당 월의 마지막일(예: 2025-11-30) 계산
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 월 범위의 완료 근무 기록 조회
        List<Attendance> attendances =
                attendanceRepository.findCompletedAttendancesByUserIdAndWorkDateBetween(userId, startDate, endDate);

        // 스토어별 집계 결과를 담을 맵 생성 (key: storeId)
        Map<Long, StoreMonthlyAccumulator> byStore = new LinkedHashMap<>();

        // 조회된 근무 기록을 하나씩 순회
        for (Attendance attendance : attendances) {
            // 방어 로직: clockIn/clockOut 중 하나라도 null이면 계산에서 제외
            if (attendance.getClockInAt() == null || attendance.getClockOutAt() == null) {
                // 다음 기록으로 넘어감
                continue; // repository에서 이미 거르지만 이중 안전장치
            }

            // 급여 계산 기준 시작 시각:
            // NORMAL -> 스케줄 시작(updatedStartTime)
            // LATE -> 실제 출근(clockInAt)을 급여 규칙에 맞게 보정
            // 그 외/예외 상황은 실제 출근 시각을 기본값으로 사용
            AttendanceStatus attendanceStatus = attendance.getStatus();
            LocalDateTime paidStartTime;
            if (attendanceStatus == AttendanceStatus.NORMAL && attendance.getShiftAssignment().getUpdatedStartTime() != null) {
                paidStartTime = attendance.getShiftAssignment().getUpdatedStartTime();
            } else if (attendanceStatus == AttendanceStatus.LATE) {
                paidStartTime = normalizePayrollTime(attendance.getClockInAt());
            } else {
                paidStartTime = attendance.getClockInAt();
            }

            // 퇴근 시각은 급여 규칙에 맞게 보정
            LocalDateTime normalizedClockOut = normalizePayrollTime(attendance.getClockOutAt());

            // 실제 근무 분 계산 (보정된 퇴근 시각 - 급여 기준 시작 시각)
            long workedMinutes = Duration.between(paidStartTime, normalizedClockOut).toMinutes();
            // 음수 분 데이터 방지: 0 미만이면 0으로 보정
            workedMinutes = Math.max(0, workedMinutes);

            // 현재 근무 기록의 스토어 멤버 정보 추출
            StoreMember member = attendance.getShiftAssignment().getMember();
            // 시급 null 방지: null이면 0원으로 처리
            int hourlyWage = member.getHourlyWage() == null ? 0 : member.getHourlyWage();

            // 스토어 ID 추출 (그룹핑 키로 사용)
            Long storeId = member.getStore().getId();
            // 스토어 이름 추출 (응답 표시용)
            String storeName = member.getStore().getName();
            // 스토어 별칭 추출 (없을 수 있음)
            String storeAlias = member.getStore().getAlias();

            // storeId 기준으로 누적기 조회, 없으면 새로 생성해서 저장
            StoreMonthlyAccumulator acc = byStore.computeIfAbsent(
                    storeId,
                    // 최초 생성 시 스토어 기본 정보 + 시급 초기값 세팅
                    id -> new StoreMonthlyAccumulator(id, storeName, storeAlias, hourlyWage)
            );

            // 현재 근무 1건(분/시급)을 해당 스토어 누적기에 반영
            acc.addWork(workedMinutes, hourlyWage);
        }

        // 스토어별 누적기(Map values)를 API 응답 DTO 리스트로 변환
        List<StoreMonthlySalaryResDto> stores = byStore.values().stream()
                // 누적기 1개를 스토어 응답 DTO 1개로 빌드
                .map(acc -> StoreMonthlySalaryResDto.builder()
                        // 스토어 ID 세팅
                        .storeId(acc.storeId)
                        // 스토어 이름 세팅
                        .storeName(acc.storeName)
                        // 스토어 별칭 세팅
                        .storeAlias(acc.storeAlias)
                        // 대표 시급 세팅
                        .hourlyWage(acc.representativeHourlyWage)
                        // 월 누적 근무 분 세팅
                        .workedMinutes(acc.workedMinutes)
                        // 월 누적 근무 시간(단순 시 단위 표시: 분/60)
                        .workedHours(acc.workedMinutes / 60)
                        // 월 누적 예상 급여 세팅
                        .estimatedPay(acc.estimatedPay)
                        // DTO 빌드 완료
                        .build())
                // 월 변경 시 카드 순서가 흔들리지 않도록 storeId 기준 고정 정렬
                .sorted(Comparator.comparingLong(StoreMonthlySalaryResDto::getStoreId))
                // List로 수집
                .collect(Collectors.toList());

        // 전체 스토어 예상 급여 합계 계산
        long totalEstimatedPay = stores.stream()
                // 각 스토어 DTO에서 estimatedPay 추출
                .mapToLong(StoreMonthlySalaryResDto::getEstimatedPay)
                // 전부 합산
                .sum();

        // 최종 월별 요약 응답 DTO 생성 후 반환
        return MonthlySalarySummaryResDto.builder()
                // 조회 기준 연도 세팅
                .year(yearMonth.getYear())
                // 조회 기준 월 세팅
                .month(yearMonth.getMonthValue())
                // 전체 예상 급여 합계 세팅
                .totalEstimatedPay(totalEstimatedPay)
                // 스토어별 집계 리스트 세팅
                .stores(stores)
                // DTO 빌드 완료
                .build();
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

    // 로그인 사용자의 비밀번호 변경
    public void changeMyPassword(Long userId, ChangePasswordReqDto request) {
        // 1) 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        // 2) 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // 3) 새 비밀번호 / 확인값 일치 검증
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
        }

        // 4) 기존 비밀번호와 동일한지 검증
        // (새 비밀번호 평문을 기존 해시와 matches로 비교)
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 5) 새 비밀번호 인코딩 후 엔티티 반영
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);

        // 6) 변경사항 DB 반영
        userRepository.save(user);
    }

    // 로그인 사용자의 프로필(이름/전화번호) 수정
    public UserInfoResDto updateMyProfile(Long userId, UpdateMyProfileReqDto request) {
        // 1) 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        // 2) 입력값 정리(trim)
        String name = request.getName() == null ? null : request.getName().trim();
        String phoneNumber = request.getPhoneNumber() == null ? null : request.getPhoneNumber().trim();

        // 3) 전화번호 형식 방어 검증
        // (DTO에서 @Pattern 검증하지만, 서비스에서도 한 번 더 검증해 안전성 확보)
        if (phoneNumber != null && !phoneNumber.matches("^[0-9]{10,11}$")) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 4) 엔티티 상태 변경
        user.updateProfile(name, phoneNumber);

        // 5) DB 반영
        User saved = userRepository.save(user);

        // 6) 수정된 사용자 정보 반환
        return UserInfoResDto.from(saved);
    }

}
