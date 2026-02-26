package com.example.shiftmate.domain.openShift.service;

import com.example.shiftmate.domain.openShift.dto.request.OpenShiftReqDto;
import com.example.shiftmate.domain.openShift.dto.response.OpenShiftApplyResDto;
import com.example.shiftmate.domain.openShift.dto.response.OpenShiftResDto;
import com.example.shiftmate.domain.openShift.entity.OpenShiftApply;
import com.example.shiftmate.domain.openShift.entity.OpenShiftRequest;
import com.example.shiftmate.domain.openShift.repository.OpenShiftApplyRepository;
import com.example.shiftmate.domain.openShift.repository.OpenShiftRequestRepository;
import com.example.shiftmate.domain.openShift.status.ApplyStatus;
import com.example.shiftmate.domain.openShift.status.RequestStatus;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.repository.ShiftTemplateRepository;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpenShiftService {
    private final OpenShiftRequestRepository openShiftRequestRepository;
    private final OpenShiftApplyRepository openShiftApplyRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final StoreRepository storeRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Transactional
    public void createOpenShift(Long storeId, @Valid OpenShiftReqDto request, Long userId) {
        // 해당 매장의 관리자인지 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getMemberRank() != StoreRank.MANAGER) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        ShiftTemplate template = shiftTemplateRepository.findById(request.getShiftTemplateId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (!template.getStore().getId().equals(store.getId())) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        if (request.getWorkDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.PAST_DATE_CANNOT_REQUEST);
        }

        OpenShiftRequest openShiftRequest = OpenShiftRequest.builder()
                .store(store)
                .shiftTemplate(template)
                .workDate(request.getWorkDate())
                .note(request.getNote())
                .requestStatus(RequestStatus.OPEN)
                .build();

        openShiftRequestRepository.save(openShiftRequest);
    }

    public List<OpenShiftResDto> getOpenShifts(Long storeId, Long userId) {
        // 해당 매장의 멤버인지 확인
        storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return openShiftRequestRepository.findAllByStoreIdAndRequestStatusInOrderByWorkDateDesc(
                        storeId,
                        List.of(RequestStatus.OPEN, RequestStatus.RECRUITING)
                ).stream()
                .map(OpenShiftResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void applyOpenShift(Long storeId, Long openShiftId, Long userId) {
        // 해당 매장의 멤버인지 확인
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 오픈시프트 조회
        OpenShiftRequest openShift = openShiftRequestRepository.findById(openShiftId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_SHIFT_NOT_FOUND));

        // 오픈시프트의 상태가 OPEN, RECRUITING이 아니면 지원 불가
        if (openShift.getRequestStatus() != RequestStatus.OPEN && openShift.getRequestStatus() != RequestStatus.RECRUITING) {
            throw new CustomException(ErrorCode.CANNOT_APPLY);
        }

        // 중복 지원 불가능
        if (openShiftApplyRepository.existsByRequestIdAndApplicantId(openShiftId, member.getId())) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        OpenShiftApply apply = OpenShiftApply.builder()
                .request(openShift)
                .applicant(member)
                .applyStatus(ApplyStatus.WAITING)
                .build();

        openShiftApplyRepository.save(apply);

        // 오픈시프트에 첫 지원자가 발생하면 상태를 RECRUITING으로 변경
        if (openShift.getRequestStatus() == RequestStatus.OPEN) {
            openShift.changeRequestStatus(RequestStatus.RECRUITING);
        }
    }

    public List<OpenShiftApplyResDto> getOpenShiftApplies(Long storeId, Long openShiftId, Long userId) {
        // 해당 매장의 관리자인지 검증
        StoreMember manager = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (manager.getMemberRank() != StoreRank.MANAGER) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 오픈시프트 조회 및 해당 매장에 속한 오픈시프트인지 검증
        OpenShiftRequest openShift = openShiftRequestRepository.findById(openShiftId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_SHIFT_NOT_FOUND));

        if (!openShift.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.OPEN_SHIFT_NOT_FOUND);
        }

        return openShiftApplyRepository.findAllByRequestId(openShiftId).stream()
                .map(OpenShiftApplyResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveApply(Long storeId, Long openShiftId, Long applyId, Long userId) {
        // 해당 매장의 관리자인지 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getMemberRank() != StoreRank.MANAGER) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        OpenShiftRequest openShift = openShiftRequestRepository.findWithTemplateById(openShiftId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_SHIFT_NOT_FOUND));

        OpenShiftApply apply = openShiftApplyRepository.findById(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!openShift.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.OPEN_SHIFT_NOT_FOUND);
        }

        if (!apply.getRequest().getId().equals(openShiftId)) {
            throw new CustomException(ErrorCode.NOT_SUBSTITUTE_APPLICATION);
        }

        // 오픈시프트 상태가 RECRUITING이 아니면 승인 불가
        // OPEN은 지원자가 없는 상태고, CLOSED/CANCELED는 마감 또는 취소된 상태기 때문
        if (openShift.getRequestStatus() != RequestStatus.RECRUITING) {
            throw new CustomException(ErrorCode.CANNOT_SELECT);
        }

        // 지원 상태가 WAITING일 때만 승인 가능
        if (apply.getApplyStatus() != ApplyStatus.WAITING) {
            throw new CustomException(ErrorCode.CANNOT_SELECT);
        }

        // 승인 처리
        apply.changeApplyStatus(ApplyStatus.ACCEPTED);
        openShift.changeRequestStatus(RequestStatus.CLOSED);

        // 확정자를 제외한 다른 지원자 거절 처리
        openShiftApplyRepository.rejectRemainingApplications(openShiftId, applyId, ApplyStatus.REJECTED, ApplyStatus.WAITING);

        // ShiftAssignment 생성 시 시간 정보 계산 추가
        ShiftTemplate template = openShift.getShiftTemplate();
        LocalDateTime startTime = openShift.getWorkDate().atTime(template.getStartTime());
        LocalDateTime endTime = openShift.getWorkDate().atTime(template.getEndTime());

        // 종료 시간이 시작 시간보다 빠르면(예: 22:00 ~ 02:00) 다음날로 처리
        if (endTime.isBefore(startTime)) {
            endTime = endTime.plusDays(1);
        }

        ShiftAssignment newShift = ShiftAssignment.builder()
                .member(apply.getApplicant())
                .shiftTemplate(template)
                .workDate(openShift.getWorkDate())
                .updatedStartTime(startTime)
                .updatedEndTime(endTime)
                .build();

        shiftAssignmentRepository.save(newShift);
    }
}