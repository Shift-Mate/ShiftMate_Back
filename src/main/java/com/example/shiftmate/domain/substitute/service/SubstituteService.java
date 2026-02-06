package com.example.shiftmate.domain.substitute.service;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.substitute.dto.request.SubstituteReqDto;
import com.example.shiftmate.domain.substitute.dto.response.SubstituteApplicationResDto;
import com.example.shiftmate.domain.substitute.dto.response.SubstituteResDto;
import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.repository.SubstituteApplicationRepository;
import com.example.shiftmate.domain.substitute.repository.SubstituteRequestRepository;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubstituteService {

    private final SubstituteRequestRepository substituteRequestRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final SubstituteApplicationRepository substituteApplicationRepository;

    @Transactional
    public void createSubstitute(Long storeId, @Valid SubstituteReqDto reqDto, Long userId) {
        // 직원 정보 조회
        StoreMember requester = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 스케줄 정보 조회
        ShiftAssignment assignment = shiftAssignmentRepository.findById(reqDto.getAssignmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND));

        // 해당 매장에 속한 스케줄이 맞는지 검증
        if (!assignment.getMember().getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.STORE_MISMATCH);
        }

        // 과거의 스케줄은 대타 요청 불가
        if(assignment.getUpdatedStartTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.PAST_SCHEDULE_CANNOT_REQUEST);
        }

        // 본인의 스케줄만 대타 요청 가능
        if(!assignment.getMember().getId().equals(requester.getId())) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 이미 요청이 있으면 중복 요청 불가
        // RequestStatus가 OPEN, PENDING인지 검증
        boolean isAlreadyRequested = substituteRequestRepository.existsByShiftAssignmentAndStatusIn(assignment, List.of(RequestStatus.OPEN, RequestStatus.PENDING));
        if(isAlreadyRequested) {
            throw new CustomException(ErrorCode.ALREADY_REQUESTED);
        }

        SubstituteRequest request = SubstituteRequest.builder()
                .shiftAssignment(assignment)
                .requester(requester)
                .status(RequestStatus.OPEN)
                .reason(reqDto.getReason())
                .build();

        substituteRequestRepository.save(request);
    }

    public List<SubstituteResDto> getOthersSubstitutes(Long storeId, Long userId) {
        // 해당 매장의 직원이 맞는지 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 본인을 제외한 다른 직원들의 대타 요청 조회
        List<SubstituteRequest> responses = substituteRequestRepository.findAllByRequester_Store_IdAndRequesterIdNotOrderByCreatedAtDesc(storeId, member.getId());

        return responses.stream()
                .map(SubstituteResDto::from)
                .collect(Collectors.toList());
    }

    public List<SubstituteResDto> getMySubstitutes(Long storeId, Long userId) {
        // 해당 매장의 직원이 맞는지 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 본인의 대타 요청만 조회
        List<SubstituteRequest> responses = substituteRequestRepository.findAllByRequesterIdOrderByCreatedAtDesc(member.getId());

        return responses.stream()
                .map(SubstituteResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelSubstitute(Long storeId, Long userId, Long requestId) {
        // 해당 매장의 직원인지 검증
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 존재하는 대타 요청인지 검증
        SubstituteRequest request = substituteRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSTITUTE_REQ_NOT_FOUND));

        // 본인의 대타 요청인지 검증
        if(!request.getRequester().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 대타 요청의 상태가 PENDING, APPROVED면 취소 불가
        if(request.getStatus().equals(RequestStatus.PENDING) ||  request.getStatus().equals(RequestStatus.APPROVED)) {
            throw new CustomException(ErrorCode.ALREADY_REQUESTED);
        }

        request.cancel();
    }

    @Transactional
    public void createApplication(Long storeId, Long requestId, Long userId) {
        // 직원 정보 조회
        StoreMember applicant = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 대타 요청 조회
        SubstituteRequest request = substituteRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSTITUTE_REQ_NOT_FOUND));

        // 해당 매장의 직원인지 검증
        if(!applicant.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND);
        }

        // 해당 매장의 대타 요청인지 검증
        if(!request.getRequester().getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.SUBSTITUTE_REQ_NOT_FOUND);
        }

        // 본인의 대타 요청에는 지원 불가
        if(request.getRequester().getId().equals(applicant.getId())) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 이미 지원했으면 중복 지원 불가
        boolean isAlreadyApply = substituteApplicationRepository.existsByRequestIdAndApplicantId(requestId, applicant.getId());
        if(isAlreadyApply) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        // 대타 요청 상태가 OPEN, PENDING일 때만 지원 가능
        if(request.getStatus() != RequestStatus.OPEN && request.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.CANNOT_APPLY);
        }

        SubstituteApplication application = SubstituteApplication.builder()
                .request(request)
                .applicant(applicant)
                .status(ApplicationStatus.WAITING)
                .build();

        substituteApplicationRepository.save(application);

        request.changeStatus();
    }

    public List<SubstituteApplicationResDto> getMyApplications(Long storeId, Long userId) {
        // 직원 조회
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return substituteApplicationRepository.findAllByApplicantId(member.getId()).stream()
                .map(SubstituteApplicationResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelApplication(Long storeId, Long applicationId, Long userId) {
        // 직원 조회
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 대타 지원 조회
        SubstituteApplication application = substituteApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // userId가 생성한 대타 지원인지 검증
        if(!application.getApplicant().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 대타 지원 상태가 WAITING이 아닐 때는 취소 불가
        if(!application.getStatus().equals(ApplicationStatus.WAITING)) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL);
        }

        // 지원 취소 처리 -> 지원 상태 변경
        application.changeStatus(ApplicationStatus.CANCELED);

        // 해당 대타 요청에 지원자가 아무도 없으면 대타 요청 상태 OPEN으로 변경
        boolean hasApplicants = substituteApplicationRepository.existsByRequestIdAndStatus(application.getRequest().getId(), ApplicationStatus.WAITING);
        if(!hasApplicants) {
            application.getRequest().setStatus(RequestStatus.OPEN);
        }
    }
}
