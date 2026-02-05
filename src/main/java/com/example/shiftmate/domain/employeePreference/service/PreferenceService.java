package com.example.shiftmate.domain.employeePreference.service;

import com.example.shiftmate.domain.employeePreference.dto.request.CreatePreferenceItemReqDto;
import com.example.shiftmate.domain.employeePreference.dto.request.CreateWeeklyPreferenceReqDto;
import com.example.shiftmate.domain.employeePreference.entity.EmployeePreference;
import com.example.shiftmate.domain.employeePreference.repository.employeePreferenceRepository;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.repository.ShiftTemplateRepository;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenceService {


    private final ShiftTemplateRepository shiftTemplateRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final employeePreferenceRepository employeePreferenceRepository;


    @Transactional
    public void createPreference(Long storeId,Long memberId, CreateWeeklyPreferenceReqDto preference) {
        // 직원을 초대할 때 선호하는 시간까지 입력하는 것으로 가정
        // 때문에 employeePreference가 존재하는지 확인하는 로직은 생략
        // 만약 요구사항이 직원 초대 후 별도의 트랜잭션으로 선호하는 시간을 입력하는거라면
        // 이미 입력된 선호시간이 잇는지 확인하는 로직 필요

        List<ShiftTemplate> template = shiftTemplateRepository.findByStoreId(storeId).orElseThrow(
            ()-> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
        );
        StoreMember member = storeMemberRepository.findById(memberId).orElseThrow(
            ()-> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND)
        );

        if(!employeePreferenceRepository.existsByMemberId(memberId)){
            List<CreatePreferenceItemReqDto> preferenceItems = preference.getPreference();
            for (CreatePreferenceItemReqDto preferenceItem : preferenceItems) {
                ArrayList<EmployeePreference> templates = new ArrayList<>();
                for (ShiftTemplate shiftTemplate : template) {
                    EmployeePreference employeePreference= EmployeePreference.builder()
                        .member(member)
                        .shiftTemplate(shiftTemplate)
                        .dayOfWeek(preferenceItem.getDayOfWeek())
                        .type(preferenceItem.getType())
                        .build();
//                   employeePreferenceRepository.save(employeePreference);
                    templates.add(employeePreference);
                }
                employeePreferenceRepository.saveAll(templates);
            }
        }else{
            throw new CustomException(ErrorCode.PREFERENCE_ALREADY_EXISTS);
        };

    }
}
