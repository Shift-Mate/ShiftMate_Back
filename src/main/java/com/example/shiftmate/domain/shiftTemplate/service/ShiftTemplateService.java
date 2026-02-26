package com.example.shiftmate.domain.shiftTemplate.service;

import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateCreateReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateShiftStaffReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.request.UpdateTemplateTypeReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.response.TemplateResDto;
import com.example.shiftmate.domain.shiftTemplate.entity.DayType;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftType;
import com.example.shiftmate.domain.shiftTemplate.entity.TemplateType;
import com.example.shiftmate.domain.shiftTemplate.repository.ShiftTemplateRepository;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftTemplateService {

    private static final long SLOT_MINUTES = 30L;

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final StoreRepository storeRepository;
    private final StoreMemberRepository storeMemberRepository;

    @Transactional
    public void createTemplate(Long storeId, TemplateCreateReqDto templateCreateReqDto,Long userId) {

        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        if (shiftTemplateRepository.existsByStoreId(storeId)) {
            throw new CustomException(ErrorCode.TEMPLATE_ALREADY_EXISTS);
        }

        Store store = storeRepository.findById(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.STORE_NOT_FOUND)
        );

        List<ShiftTemplate> shifts = new ArrayList<>();

        if (Boolean.TRUE.equals(templateCreateReqDto.getPeak())) {
            LocalTime pStart = templateCreateReqDto.getPeakStartTime();
            LocalTime pEnd = templateCreateReqDto.getPeakEndTime();

            if (!pStart.isBefore(pEnd)) {
                throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
            }

            // 1. HighService (분리형): 기본 n교대 + 피크 타임 추가
            shifts.addAll(createHighServiceShifts(store, pStart, pEnd));

            // 2. CostSaver (중첩형): 피크 타임에 맞춰 n교대 시간 조정
            shifts.addAll(createCostSaverShifts(store, pStart, pEnd));

        } else {
            // 피크가 없는 경우: 기본 n교대만 생성 (TemplateType null 혹은 별도 처리)
            shifts.addAll(createBaseShifts(store, null));
        }

        shiftTemplateRepository.saveAll(shifts);

    }

    // 기본 n교대 생성 로직 (시간 변형 없음)
    private List<ShiftTemplate> createBaseShifts(Store store, TemplateType templateType) {
        List<ShiftTemplate> shifts = new ArrayList<>();
        List<LocalTime> boundaries = buildShiftBoundaries(
            store.getOpenTime(),
            store.getCloseTime(),
            store.getNShifts()
        );

        for (int i = 0; i < store.getNShifts(); i++) {
            LocalTime currentStart = boundaries.get(i);
            LocalTime currentEnd = boundaries.get(i + 1);
            shifts.add(ShiftTemplate.builder()
                           .store(store)
                           .name("Shift " + (i + 1))
                           .startTime(currentStart)
                           .endTime(currentEnd)
                           .shiftType(ShiftType.NORMAL)
                           .dayType(DayType.WEEKDAY)
                           .templateType(templateType)
                           .build());
        }
        return shifts;
    }

    private List<ShiftTemplate> createHighServiceShifts(Store store, LocalTime pStart,
        LocalTime pEnd) {
        // 기존 Normal Shift 생성 (Tag: HIGHSERVICE)
        List<ShiftTemplate> shifts = createBaseShifts(store, TemplateType.HIGHSERVICE);

        // Peak Shift 추가
        shifts.add(ShiftTemplate.builder()
                       .store(store)
                       .name("Peak")
                       .startTime(pStart)
                       .endTime(pEnd)
                       .shiftType(ShiftType.PEAK)
                       .dayType(DayType.WEEKDAY)
                       .templateType(TemplateType.HIGHSERVICE)
                       .build());
        return shifts;
    }

    private List<ShiftTemplate> createCostSaverShifts(Store store, LocalTime pStart,
        LocalTime pEnd) {
        List<ShiftTemplate> shifts = new ArrayList<>();
        LocalTime openTime = store.getOpenTime();
        LocalTime closeTime = store.getCloseTime();

        validateTimeRange(openTime, closeTime);
        List<LocalTime> boundaries = buildShiftBoundaries(openTime, closeTime, store.getNShifts());
        LocalTime previousEnd = openTime;

        for (int i = 0; i < store.getNShifts(); i++) {
            LocalTime currentStart = boundaries.get(i);
            LocalTime currentEnd = boundaries.get(i + 1);

            // --- CostSaver용 시간 조정 로직 ---
            LocalTime adjustedStart = currentStart;
            LocalTime adjustedEnd = currentEnd;

            // 해당 Shift가 피크 타임과 겹치는지 확인하여 시간 조정
            // 1. Shift 시작 시간이 피크 구간 안에 포함되면 -> 피크 시작 시간으로 당김 (중첩 확장)
            if (isTimeBetween(currentStart, pStart, pEnd)) {
                adjustedStart = pStart;
            }
            // 2. Shift 종료 시간이 피크 구간 안에 포함되면 -> 피크 종료 시간으로 늘림 (중첩 확장)
            if (isTimeBetween(currentEnd, pStart, pEnd)) {
                adjustedEnd = pEnd;
            }

            // 일반 시프트끼리는 겹치지 않도록 이전 종료 시각 이후로 보정
            if (adjustedStart.isBefore(previousEnd)) {
                adjustedStart = previousEnd;
            }
            if (!adjustedEnd.isAfter(adjustedStart)) {
                adjustedEnd = currentEnd.isAfter(adjustedStart) ? currentEnd : adjustedStart.plusMinutes(1);
            }
            if (adjustedEnd.isAfter(closeTime)) {
                adjustedEnd = closeTime;
            }

            shifts.add(ShiftTemplate.builder()
                           .store(store)
                           .name("Shift " + (i + 1))
                           .startTime(adjustedStart)
                           .endTime(adjustedEnd)
                           .shiftType(ShiftType.NORMAL)
                           .dayType(DayType.WEEKDAY)
                           .templateType(TemplateType.COSTSAVER)
                           .build());

            previousEnd = adjustedEnd;
        }
        return shifts;
    }

    private List<LocalTime> buildShiftBoundaries(LocalTime openTime, LocalTime closeTime, int nShifts) {
        validateTimeRange(openTime, closeTime);

        long openMinutes = toMinutes(openTime);
        long closeMinutes = toMinutes(closeTime);
        long totalMinutes = Duration.between(openTime, closeTime).toMinutes();

        List<LocalTime> boundaries = new ArrayList<>();
        boundaries.add(openTime);

        // 영업 시간이 너무 짧으면 분 단위 균등 분할로 fallback
        if (totalMinutes < SLOT_MINUTES * nShifts) {
            for (int i = 1; i < nShifts; i++) {
                long minute = openMinutes + Math.round((double) totalMinutes * i / nShifts);
                boundaries.add(toLocalTime(minute));
            }
            boundaries.add(closeTime);
            return boundaries;
        }

        long previousBoundary = openMinutes;
        for (int i = 1; i < nShifts; i++) {
            long ideal = openMinutes + Math.round((double) totalMinutes * i / nShifts);
            long snapped = roundToNearestSlot(ideal);

            long remainingShifts = nShifts - i;
            long minAllowed = previousBoundary + SLOT_MINUTES;
            long maxAllowed = closeMinutes - (SLOT_MINUTES * remainingShifts);

            long clamped = Math.max(minAllowed, Math.min(snapped, maxAllowed));
            boundaries.add(toLocalTime(clamped));
            previousBoundary = clamped;
        }

        boundaries.add(closeTime);
        return boundaries;
    }

    private long roundToNearestSlot(long minutes) {
        long remainder = minutes % SLOT_MINUTES;
        if (remainder < SLOT_MINUTES / 2) {
            return minutes - remainder;
        }
        return minutes + (SLOT_MINUTES - remainder);
    }

    private long toMinutes(LocalTime time) {
        return (long) time.getHour() * 60 + time.getMinute();
    }

    private LocalTime toLocalTime(long minutes) {
        int hour = (int) (minutes / 60);
        int minute = (int) (minutes % 60);
        return LocalTime.of(hour, minute);
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }
    }

    private boolean isTimeBetween(LocalTime target, LocalTime start, LocalTime end) {
        // start < end 전제
        return target.isAfter(start) && target.isBefore(end);
    }

    public List<TemplateResDto> getTemplate(Long storeId) {
        List<ShiftTemplate> shifts = shiftTemplateRepository.findByStoreId(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
        );

        return shifts.stream()
                   .map(TemplateResDto::from)
                   .collect(Collectors.toList());
    }

    @Transactional
    public TemplateResDto shiftStaff(Long templateId, TemplateShiftStaffReqDto templateShiftStaffReqDto) {


        ShiftTemplate template = shiftTemplateRepository.findById(templateId).orElseThrow(
            () -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
        );

        template.shiftStaff(templateShiftStaffReqDto.getRequiredStaff());
        String shiftName = templateShiftStaffReqDto.getName();
        if (shiftName != null && !shiftName.trim().isEmpty()) {
            template.updateName(shiftName.trim());
        }

        return TemplateResDto.from(template);
    }

    @Transactional
    public void updateTemplateType(Long storeId,
        UpdateTemplateTypeReqDto updateTemplateTypeReqDto,
        Long userId) {

        Store store = storeRepository.findById(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.STORE_NOT_FOUND)
        );

        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }


        store.updateTemplateType(updateTemplateTypeReqDto.getTemplateType());
    }

    public List<TemplateResDto> getTemplateByType(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.STORE_NOT_FOUND)
        );

        if (store.getTemplateType().equals(TemplateType.COSTSAVER)) {
            List<ShiftTemplate> template = shiftTemplateRepository.findByStoreIdAndTemplateType(
                storeId, TemplateType.COSTSAVER).orElseThrow(
                () -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
            );
            return template.stream().map(TemplateResDto::from).collect(Collectors.toList());
        } else if (store.getTemplateType().equals(TemplateType.HIGHSERVICE)) {
            List<ShiftTemplate> template = shiftTemplateRepository.findByStoreIdAndTemplateType(
                storeId, TemplateType.HIGHSERVICE).orElseThrow(
                () -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND)
            );
            return template.stream().map(TemplateResDto::from).collect(Collectors.toList());
        } else {
            throw new CustomException(ErrorCode.TYPE_NOT_FOUND);
        }

    }

    @Transactional
    public void removeUnusedShiftTemplates(Long storeId,Long userId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
            () -> new CustomException(ErrorCode.STORE_NOT_FOUND)
        );
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }


        List<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findByStoreId(storeId)
                                                .orElseThrow(
                                                    () -> new CustomException(
                                                        ErrorCode.TEMPLATE_NOT_FOUND)
                                                );

        List<ShiftTemplate> unusedTemplates = shiftTemplate.stream()
//            .filter(template -> !template.getTemplateType().equals(store.getTemplate_type()))
                                                  .filter(
                                                      template -> !template.getTemplateType()
                                                                       .equals(
                                                                           store.getTemplateType()))
                                                  .collect(Collectors.toList());

        shiftTemplateRepository.deleteAll(unusedTemplates);
    }

    public void deleteTemplate(Long storeId,Long userId) {

        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        List<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findByStoreId(storeId)
                                                .orElseThrow(
                                                    () -> new CustomException(
                                                        ErrorCode.TEMPLATE_NOT_FOUND)
                                                );

        shiftTemplateRepository.deleteAll(shiftTemplate);
    }
}
