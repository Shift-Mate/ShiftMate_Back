package com.example.shiftmate.domain.store.service;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.domain.store.repository.StoreRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.domain.user.service.FileStorageService;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final FileStorageService fileStorageService;

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final String STORE_IMAGE_PREVIEW_PATH_FORMAT = "/api/stores/%d/image/preview";
    private static final Set<String> ALLOWED_STORE_IMAGE_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpg",
            "image/jpeg"
    );

    @Transactional
    public StoreResDto create(StoreReqDto request, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 영업 시작 시간은 종료 시간보다 빨라야 함
        if (!request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Store 엔티티 생성
        Store store = Store.builder()
            .name(request.getName())
            .location(request.getLocation())
            .openTime(request.getOpenTime())
            .closeTime(request.getCloseTime())
            .nShifts(request.getNShifts())
            .brn(request.getBrn())
            .user(user)
            .alias(request.getAlias())
            .monthlySales(request.getMonthlySales())
            .build();

        // 저장
        Store savedStore = storeRepository.save(store);

        // 생성자를 매장 멤버로 자동 등록 (MANAGER)
        StoreMember creatorMember = StoreMember.builder()
            .store(savedStore)
            .user(user)
            .role(StoreRole.MANAGER)
            .memberRank(StoreRank.MANAGER)
            .department(Department.HALL)
            .hourlyWage(null)
            .minHoursPerWeek(0)
            .status(MemberStatus.ACTIVE)
            .build();
        storeMemberRepository.save(creatorMember);

        // DTO 변환
        return toResponseDto(savedStore);
    }

    // 단일 매장 조회, GET /stores/{storeId}
    public StoreResDto findById(Long storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return toResponseDto(store);
    }

    // 로그인한 유저가 속한 매장 목록 조회, GET /stores
    public List<StoreResDto> findStoresByUserId(Long userId) {
        List<StoreMember> storeMembers = storeMemberRepository.findByUserId(userId);

        if (storeMembers.isEmpty()) {
            return List.of();
        }

        return storeMembers.stream()
                .map(StoreMember::getStore)
                .filter(store -> !store.isDeleted())
                .collect(Collectors.toMap(Store::getId, store -> store, (a, b) -> a))
                .values()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }
    // Store Update, PUT /stores/{storeId}
    @Transactional
    public StoreResDto update(Long storeId, StoreReqDto request, Long userId) {
        // store 조회 (삭제되지 않은 것만)
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 매장의 MANAGER만 수정 가능
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        // 영업 시작 시간은 종료 시간보다 빨라야 함 (변경된 경우만 검사하려면 request 값 기준)
        if (request.getOpenTime() != null && request.getCloseTime() != null
                && !request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }
        
        // store update
        store.update(
            request.getName(),
            request.getLocation(),
            request.getOpenTime(),
            request.getCloseTime(),
            request.getNShifts(),
            request.getBrn(),
            null, // user는 업데이트x
            request.getAlias(),
            request.getMonthlySales()
        );

        // dto 변환
        return toResponseDto(store);

    }

    // Store Delete(Soft Delete)
    // DELETE /stores/{storeId}
    @Transactional
    public void delete(Long storeId, Long userId) {
        // store 조회 (삭제되지 않은 것만)
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 매장의 MANAGER만 삭제 가능
        if (!storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, userId, StoreRole.MANAGER)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        // Soft Delete 실행
        store.softDelete();
    }


    @Transactional
    public StoreResDto uploadStoreImage(Long storeId, Long managerUserId, MultipartFile file) {
        // 1) 파일 존재/빈 파일 방어
        //    프론트에서 실수로 빈 파일이 들어와도 서버에서 안전하게 막는다.
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 2) 이미지 전용 검증
        //    가게 대표 이미지는 문서와 다르게 "이미지 MIME"만 허용한다.
        validateStoreImageFile(file);

        // 3) 대상 매장 조회
        //    soft delete 된 매장은 수정 대상에서 제외한다.
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 4) 권한 검증
        //    해당 매장의 MANAGER만 이미지를 변경할 수 있다.
        validateManagerAccess(storeId, managerUserId);

        // 5) 저장소에 실제 파일 저장
        //    FileStorageService의 첫 번째 인자는 "폴더 구분용 숫자 키"로 활용한다.
        //    문서에서는 userId를 넣지만, 여기서는 storeId를 넣어 매장 기준 폴더를 만든다.
        String typeFolder = "store-image";
        FileStorageService.StoredFile stored = fileStorageService.save(storeId, typeFolder, file);

        // 6) 기존 이미지 경로를 먼저 보관한다.
        //    새 메타데이터 반영 후 "커밋 완료 시점"에 old 파일을 지우기 위해 필요하다.
        String oldImagePath = store.hasImage() ? store.getImagePath() : null;

        // 7) DB 메타데이터 교체
        //    프론트 노출 URL은 저장소 URL이 아니라 preview API URL로 내려줄 예정이라
        //    DB에는 filePath/파일메타만 저장한다.
        store.replaceImage(
                stored.storedPath(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        // 8) old 파일 삭제는 트랜잭션 커밋 이후에 수행한다.
        //    커밋 전에 삭제하면, 이후 커밋 실패 시 DB는 롤백되고 파일만 사라져 불일치가 발생할 수 있다.
        //    afterCommit에서 정리하면 DB 상태가 확정된 뒤에만 old 파일을 삭제한다.
        if (oldImagePath != null && !oldImagePath.isBlank()
                && !oldImagePath.equals(stored.storedPath())
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 스토리지 delete는 보조 정리 작업이다.
                    // 실패하더라도 사용자 요청 자체는 이미 성공이므로 예외 전파 없이 best-effort로 처리한다.
                    try {
                        fileStorageService.delete(oldImagePath);
                    } catch (Exception ignored) {
                        // no-op
                    }
                }
            });
        }

        // 9) 응답 DTO 반환
        //    업로드 직후 화면에서 즉시 반영할 수 있도록 최신 DTO를 반환한다.
        return toResponseDto(store);
    }

    @Transactional(readOnly = true)
    public FileStorageService.DownloadedFile previewStoreImage(Long storeId) {
        // 1) 활성 매장만 조회한다.
        //    soft delete 된 매장은 미리보기 대상에서 제외한다.
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 2) 매장 이미지 등록 여부를 확인한다.
        //    imagePath가 없으면 실제 파일을 찾을 수 없으므로 명확한 도메인 에러를 반환한다.
        if (!store.hasImage()) {
            throw new CustomException(ErrorCode.STORE_IMAGE_NOT_FOUND);
        }

        // 3) 저장소(local/s3) 구현체에 실제 파일 읽기를 위임한다.
        //    컨트롤러는 내려받은 스트림을 inline 응답으로 반환해 브라우저에서 직접 렌더링한다.
        return fileStorageService.download(
                store.getImagePath(),
                store.getImageOriginalFileName(),
                store.getImageContentType()
        );
    }

    @Transactional
    public void deleteStoreImage(Long storeId, Long managerUserId) {
        // 1) 매장 조회 + 권한 검증
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        validateManagerAccess(storeId, managerUserId);

        // 2) 현재 이미지 존재 확인
        if (!store.hasImage()) {
            throw new CustomException(ErrorCode.STORE_IMAGE_NOT_FOUND);
        }

        // 3) 삭제 대상 파일 경로를 보관하고 DB 메타를 먼저 제거한다.
        //    커밋 이후에만 실제 파일을 삭제하기 위해 old 경로를 별도로 저장한다.
        String imagePathToDelete = store.getImagePath();

        // 4) DB 메타 제거
        store.clearImage();

        // 5) 파일 삭제는 트랜잭션 커밋 이후에 수행한다.
        //    커밋 실패 시 DB 롤백이 발생하므로 파일도 그대로 남아야 일관성이 유지된다.
        if (imagePathToDelete != null && !imagePathToDelete.isBlank()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 파일 정리는 best-effort로 처리한다.
                    // 커밋 완료 후 부가 작업이므로 삭제 실패를 API 실패로 전파하지 않는다.
                    try {
                        fileStorageService.delete(imagePathToDelete);
                    } catch (Exception ignored) {
                        // no-op
                    }
                }
            });
        }
    }

    private void validateManagerAccess(Long storeId, Long userId) {
        // 매장 수정 계열 API 공통 권한검사
        // MANAGER가 아니면 업로드/삭제 모두 거부한다.
        boolean managerInStore =
                storeMemberRepository.existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(
                        storeId, userId, StoreRole.MANAGER
                );

        if (!managerInStore) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    private void validateStoreImageFile(MultipartFile file) {
        // 1) 크기 제한
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }

        // 2) MIME 제한
        //    브라우저/모바일 업로드 호환성을 위해 jpg/jpeg/png만 허용한다.
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean allowed = ALLOWED_STORE_IMAGE_CONTENT_TYPES.contains(ct);

        if (!allowed) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    // Store -> StoreResDto 변환
    private StoreResDto toResponseDto(Store store) {
        int storeMemberCount = storeMemberRepository.countByStoreIdAndDeletedAtIsNull(store.getId());

        // 프론트가 <img src>로 바로 사용할 수 있게
        // S3 직링크가 아니라 "백엔드 preview 엔드포인트"를 내려준다.
        String previewUrl = store.hasImage()
                ? String.format(STORE_IMAGE_PREVIEW_PATH_FORMAT, store.getId())
                : null;

        return StoreResDto.builder()
            .id(store.getId())
            .name(store.getName())
            .alias(store.getAlias())
            .openTime(store.getOpenTime())
            .closeTime(store.getCloseTime())
            .createdAt(store.getCreatedAt())
            .updatedAt(store.getUpdatedAt())
            .monthlySales(store.getMonthlySales())
            .storeMemberCount(storeMemberCount)
            .location(store.getLocation())
            .imageUrl(previewUrl)
            .build();
    }






}
