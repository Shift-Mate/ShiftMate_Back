package com.example.shiftmate.domain.substitute.service;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.substitute.dto.request.SubstituteReqDto;
import com.example.shiftmate.domain.substitute.dto.response.SubstituteResDto;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.repository.SubstituteRequestRepository;
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

}
