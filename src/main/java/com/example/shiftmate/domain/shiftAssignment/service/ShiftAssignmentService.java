package com.example.shiftmate.domain.shiftAssignment.service;

import com.example.shiftmate.domain.employeePreference.entity.EmployeePreference;
import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import com.example.shiftmate.domain.employeePreference.repository.EmployeePreferenceRepository;
import com.example.shiftmate.domain.shiftAssignment.dto.response.ScheduleResDto;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.repository.ShiftTemplateRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentService {


    private final EmployeePreferenceRepository employeePreferenceRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    @Transactional
    public void createSchedule(Long storeId, LocalDate weekStartDate) {

        if (shiftAssignmentRepository.existsByWorkDate(weekStartDate)) {
            throw new CustomException(ErrorCode.WEEK_ALREADY_EXISTS);
        }

        if (!(weekStartDate.getDayOfWeek() == DayOfWeek.MONDAY)) {
            throw new CustomException(ErrorCode.NOT_MONDAY_START_DATE);
        }

        // 로직을 돌리기 전 가능한 인원에 대한 전처리 작업
        List<EmployeePreference> sortMemberList = sortByScarceAvailability(storeId);

        // 최종 시간표 저장 리스트
        List<ShiftAssignment> finalAssignment = new ArrayList<>();

        //근무별 채워진 인원 수
        Map<Long , Integer>filledCountPerShift = new HashMap<>();

        for (EmployeePreference employee : sortMemberList) {
            ShiftTemplate template = employee.getShiftTemplate();
            StoreMember member = employee.getMember();

            int currentFill = filledCountPerShift.getOrDefault(template.getId() , 0);

            if(currentFill < template.getRequiredStaff()){
                // 근무할 실제 날짜 계산 (선호도에 명시된 요일 기준)
                LocalDate workDate = weekStartDate.plusDays(employee.getDayOfWeek().getValue() - 1);


                ShiftAssignment newAssignment = ShiftAssignment.builder()
                                                    .member(member)
                                                    .shiftTemplate(template)
                                                    .workDate(workDate)
                                                    .build();

                finalAssignment.add(newAssignment);

                filledCountPerShift.put(template.getId(), currentFill+1);
            }
        }

        // 최종 리스트에 저장하기
        shiftAssignmentRepository.saveAll(finalAssignment);



    }

    //불가능한 선호도를 제외하고, 요일별 가용 인원이 적은 순서로 정렬
    // 요일말고 Shift별 인원 파악 필요
    private List<EmployeePreference> sortByScarceAvailability(Long storeId) {
        // 가게에 해당하는 Shift 목록 조회
        List<ShiftTemplate> templates = shiftTemplateRepository.findByStoreId(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
        );

        //지금 가게에 해당하는 Template의 id를 배열 형태로 추출
        List<Long> templateIds = templates.stream()
                                     .map(ShiftTemplate::getId)
                                     .toList();


        // 근무자의 선호도 테이블에서 불가능한 정보를 제외하고 가게 template에 해당하는 선호 컬럼 모두 조회
        List<EmployeePreference> employeePreferences = employeePreferenceRepository
                                                           .findByTypeNotAndShiftTemplate_IdIn(
                                                               PreferenceType.UNAVAILABLE, templateIds);

        // employeePreferences 해당 List 배열에서 요일별로 가능한 시간이 적은 요일을 파악
        // 파악후 해당 시간에 근무 가능한 인원이 앞으로 오도록 정렬

        // 각 요일별 근무 가능한 직원들의 ID를 중복 없이 저장 -> HashMap 사용
        // 요일별 말구 shift별 인원을 집계하는 로직 필요
//        Map<DayOfWeek, Set<Long>> availableMembersPerDay = new HashMap<>();
        // 이때 Key Long은 ShiftId
        Map<Long , Set<Long>> availableMembersPerShift = new HashMap<>();
        for (EmployeePreference employeePreference : employeePreferences){
//            availableMembersPerDay.put(employeePreference.getDayOfWeek(),employeePreference.getMember().getId())
            //Todo: computeIfAbsent -> set이 있으면 기존 set 사용 없으면 새로 생성 나머지 옵션들의 차이 정리 필요
            availableMembersPerShift.computeIfAbsent(employeePreference.getShiftTemplate().getId(),k -> new HashSet<>())
                .add(employeePreference.getMember().getId());
        }
        // Shift별 몇명의 인원이 희망하는지 체크
//        Map<DayOfWeek , Integer> availabilityCountPerDay = new HashMap<>();
        Map<Long , Integer> availabilityCountPerShift = new HashMap<>();

        availableMembersPerShift.forEach((shiftId, members) ->{
            availabilityCountPerShift.put(shiftId,members.size());
        });

        // 집계된 인원수를 기준으로 employeePreferences 리스트 정렬
        employeePreferences.sort((pref1, pref2) ->{
            int count1 = availabilityCountPerShift.getOrDefault(pref1.getShiftTemplate().getId(), 0);
            int count2 = availabilityCountPerShift.getOrDefault(pref2.getShiftTemplate().getId(), 0);

            // 인원수가 적은 쪽이 우선 (오름차순)
            int compare = Integer.compare(count1, count2);

            // 인원수가 같다면 2차 정렬
            if (compare == 0) {
                // PREFERRED 타입이 NATURAL 타입보다 우선
                 return pref2.getType().compareTo(pref1.getType());
                }

            return compare;
        });
        return employeePreferences;
    }
}
