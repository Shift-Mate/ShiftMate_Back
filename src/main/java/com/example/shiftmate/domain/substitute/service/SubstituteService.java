package com.example.shiftmate.domain.substitute.service;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
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

    public List<SubstituteResDto> getAllSubstitutes(Long storeId, Long userId) {
        // 해당 매장의 관리자가 맞는지 검증
        verifyManager(storeId, userId);

        // 모든 대타 요청 조회
        List<SubstituteRequest> responses = substituteRequestRepository.findAll();
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

        // 대타 요청의 상태가 APPROVED면 취소 불가
        if(request.getStatus() == RequestStatus.APPROVED) {
            throw new CustomException(ErrorCode.ALREADY_REQUESTED);
        }

        request.changeStatus(RequestStatus.REQUESTER_CANCELED);

        // 대타 요청 취소 시 WAITING 상태의 지원이 있으면 지원 상태 REJECT로 변경
        List<SubstituteApplication> applications = substituteApplicationRepository.findAllByRequestId(requestId);
        for(SubstituteApplication application : applications) {
            if(application.getStatus() == ApplicationStatus.WAITING) {
                application.changeStatus(ApplicationStatus.REJECTED);
            }
        }
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

        // 부서가 다르면 지원 불가
        if(request.getRequester().getDepartment() != applicant.getDepartment()) {
            throw new CustomException(ErrorCode.DEPARTMENT_MISMATCH);
        }

        SubstituteApplication application = SubstituteApplication.builder()
                .request(request)
                .applicant(applicant)
                .status(ApplicationStatus.WAITING)
                .build();

        substituteApplicationRepository.save(application);

        if(request.getStatus() == RequestStatus.OPEN) {
            request.changeStatus(RequestStatus.PENDING);
        }
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
        if(application.getStatus() != ApplicationStatus.WAITING) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL);
        }

        // 지원 취소 처리 -> 지원 상태 변경
        application.changeStatus(ApplicationStatus.CANCELED);

        // 해당 대타 요청에 지원자가 아무도 없으면 대타 요청 상태 OPEN으로 변경
        boolean hasApplicants = substituteApplicationRepository.existsByRequestIdAndStatus(application.getRequest().getId(), ApplicationStatus.WAITING);
        if(!hasApplicants) {
            application.getRequest().changeStatus(RequestStatus.OPEN);
        }
    }

    public List<SubstituteApplicationResDto> getApplications(Long storeId, Long requestId, Long userId) {
        // 관리자인지 검증
        verifyManager(storeId, userId);

        // 특정 대타 요청에 대한 지원자 목록 조회
        return substituteApplicationRepository.findAllByRequestId(requestId).stream()
                .map(SubstituteApplicationResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveApplication(Long storeId, Long requestId, Long applicationId, Long userId) {
        // 관리자 검증
        verifyManager(storeId, userId);

        // 대타 요청 조회
        SubstituteRequest request = substituteRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSTITUTE_REQ_NOT_FOUND));

        // 대타 지원 조회
        SubstituteApplication application = substituteApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // 해당 대타 요청에 대한 지원이 맞는지 검증
        if(!application.getRequest().getId().equals(requestId)) {
            throw new CustomException(ErrorCode.NOT_SUBSTITUTE_APPLICATION);
        }

        // 대타 지원 상태 변경
        application.changeStatus(ApplicationStatus.SELECTED);

        // 대타 요청 상태 변경
        request.changeStatus(RequestStatus.APPROVED);

        // 나머지 지원자들 상태 reject로 변경
        List<SubstituteApplication> allApp = substituteApplicationRepository.findAllByRequestId(requestId);
        for(SubstituteApplication app: allApp) {
            if(!app.getId().equals(applicationId) && app.getStatus() == ApplicationStatus.WAITING) {
                app.changeStatus(ApplicationStatus.REJECTED);
            }
        }

        // 해당 스케줄의 담당자를 지원자로 변경
        ShiftAssignment assignment = request.getShiftAssignment();
        assignment.changeMember(application.getApplicant());
    }

    @Transactional
    public void rejectApplication(Long storeId, Long requestId, Long applicationId, Long userId) {
        // 관리자인지 검증
        verifyManager(storeId, userId);

        // 해당 대타 요청의 지원인지 확인
        SubstituteApplication application = substituteApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(!application.getRequest().getId().equals(requestId)) {
            throw new CustomException(ErrorCode.NOT_SUBSTITUTE_APPLICATION);
        }

        // 해당 지원 상태가 WAITING이 아니면 거절 불가
        if(application.getStatus() != ApplicationStatus.WAITING) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL);
        }

        // 해당 지원 상태 reject로 변경
        application.changeStatus(ApplicationStatus.REJECTED);

        // 해당 요청의 유효한 지원이 0개가 되면 요청의 상태를 pending -> open으로 변경
        boolean hasApplicants = substituteApplicationRepository.existsByRequestIdAndStatus(application.getRequest().getId(), ApplicationStatus.WAITING);
        if(!hasApplicants) {
            if(application.getRequest().getStatus() == RequestStatus.PENDING) {
                application.getRequest().changeStatus(RequestStatus.OPEN);
            }
        }
    }

    @Transactional
    public void managerCancelRequest(Long storeId, Long requestId, Long userId) {
        // 관리자인지 검증
        verifyManager(storeId, userId);

        // 해당 요청 조회
        SubstituteRequest request = substituteRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSTITUTE_REQ_NOT_FOUND));

        // 요청 상태 MANAGER_CANCELED로 변경
        // 요청 상태가 APPROVED, REQUESTER_CANCELED, MANAGER_CANCELED면 요청 취소 불가
        if(request.getStatus() == RequestStatus.APPROVED || request.getStatus() == RequestStatus.REQUESTER_CANCELED || request.getStatus() == RequestStatus.MANAGER_CANCELED) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL);
        }
        request.changeStatus(RequestStatus.MANAGER_CANCELED);

        // 해당 요청에 있는 모든 지원 상태 REJECTED로 변경
        List<SubstituteApplication> allApp = substituteApplicationRepository.findAllByRequestId(requestId);
        for(SubstituteApplication app: allApp) {
            app.changeStatus(ApplicationStatus.REJECTED);
        }
    }

    // 매장의 관리자인지 검증하는 메서드
    public void verifyManager(Long storeId, Long userId) {
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getMemberRank() != StoreRank.MANAGER) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }
    }
}
