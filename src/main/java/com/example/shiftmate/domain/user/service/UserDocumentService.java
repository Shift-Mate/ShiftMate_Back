package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.dto.response.UserDocumentResDto;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.entity.UserDocument;
import com.example.shiftmate.domain.user.entity.UserDocumentType;
import com.example.shiftmate.domain.user.repository.UserDocumentRepository;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDocumentService {

    private final UserRepository userRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final FileStorageService fileStorageService;
    private final StoreMemberRepository storeMemberRepository;

    public UserDocumentResDto uploadMyDocument(Long userId, UserDocumentType type, MultipartFile file) {
        // 1) 파일 존재/빈파일 방어
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 2) 파일 크기/형식 검증
        validateFile(file);

        // 3) 소유자 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 4) 저장소 폴더명을 enum 값 기반으로 생성
        //    IDENTIFICATION -> identification
        String typeFolder = type.name().toLowerCase();

        // 5) 실제 파일 저장
        FileStorageService.StoredFile stored = fileStorageService.save(userId, typeFolder, file);

        // 6) 타입당 1개 정책
        //    기존 문서가 있으면 기존 파일 삭제 후 row 메타 교체
        UserDocument existing = userDocumentRepository.findByUserIdAndType(userId, type).orElse(null);
        if (existing != null) {
            fileStorageService.delete(existing.getFilePath());

            existing.replaceFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    stored.storedPath(),
                    stored.fileUrl()
            );
            return UserDocumentResDto.from(existing);
        }

        // 7) 기존 문서가 없으면 신규 row 생성
        UserDocument saved = userDocumentRepository.save(
                UserDocument.builder()
                        .user(user)
                        .type(type)
                        .originalFileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .filePath(stored.storedPath())
                        .fileUrl(stored.fileUrl())
                        .build()
        );

        return UserDocumentResDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDocumentResDto> getMyDocuments(Long userId) {
        // 본인 문서 전체 조회
        return userDocumentRepository.findByUserId(userId).stream()
                .map(UserDocumentResDto::from)
                .toList();
    }

    public void deleteMyDocument(Long userId, UserDocumentType type) {
        // 타입 문서 1건 조회 후 삭제
        UserDocument doc = userDocumentRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_NOT_FOUND));

        // 실제 파일 먼저 제거
        fileStorageService.delete(doc.getFilePath());

        // DB 메타 제거
        userDocumentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public FileStorageService.DownloadedFile downloadMyDocument(Long userId, UserDocumentType type) {
        // 1) 본인 + 타입으로 문서 메타 조회
        //    row 자체가 없으면 404 처리한다.
        UserDocument doc = userDocumentRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_NOT_FOUND));

        // 2) 저장소 구현(local/s3)에 다운로드를 위임한다.
        //    실제 파일 읽기 방식은 구현체가 책임진다.
        return fileStorageService.download(
                doc.getFilePath(),
                doc.getOriginalFileName(),
                doc.getContentType()
        );
    }

    @Transactional(readOnly = true)
    public List<UserDocumentResDto> getMemberDocumentsForManager(Long managerUserId, Long storeId, Long memberUserId) {
        // 1) 요청자가 진짜 "해당 store의 MANAGER"인지 확인한다.
        //    다른 매장 매니저거나 일반 직원이면 즉시 차단한다.
        validateManagerAccessToMember(managerUserId, storeId, memberUserId);

        // 2) 권한 검증 통과 후에만 대상 멤버 문서 목록을 조회한다.
        //    보건증/신분증이 있으면 리스트로 내려가고, 없으면 빈 리스트가 내려간다.
        return userDocumentRepository.findByUserId(memberUserId).stream()
                .map(UserDocumentResDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FileStorageService.DownloadedFile downloadMemberDocumentForManager(
            Long managerUserId,
            Long storeId,
            Long memberUserId,
            UserDocumentType type
    ) {
        // 1) 먼저 "같은 스토어 소속 + 매니저 권한"을 검증한다.
        //    이 검증이 없으면 임의 userId로 타인 문서를 내려받을 수 있으므로 필수다.
        validateManagerAccessToMember(managerUserId, storeId, memberUserId);

        // 2) 대상 멤버가 해당 타입 문서를 실제로 업로드했는지 확인한다.
        //    없으면 404(DOCUMENT_NOT_FOUND)로 명확하게 응답한다.
        UserDocument doc = userDocumentRepository.findByUserIdAndType(memberUserId, type)
                .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_NOT_FOUND));

        // 3) 실제 파일 읽기는 storage 구현체(local/s3)에 위임한다.
        //    서비스는 "권한/도메인 검증"에 집중하고 저장소 구현 세부사항은 감춘다.
        return fileStorageService.download(
                doc.getFilePath(),
                doc.getOriginalFileName(),
                doc.getContentType()
        );
    }

    private void validateManagerAccessToMember(Long managerUserId, Long storeId, Long memberUserId) {
        // 1) 요청자가 해당 store의 MANAGER인지 확인한다.
        //    role + deletedAtIsNull 조건으로 "현재 유효한 매니저"만 허용한다.
        boolean managerInStore = storeMemberRepository
                .existsByStoreIdAndUserIdAndRoleAndDeletedAtIsNull(storeId, managerUserId, StoreRole.MANAGER);

        if (!managerInStore) {
            throw new CustomException(ErrorCode.STORE_MEMBER_ACCESS_DENIED);
        }

        // 2) 대상 멤버가 같은 store에 실제 소속되어 있는지 확인한다.
        //    다른 가게 직원 문서를 조회/다운로드하지 못하게 막는다.
        boolean memberInStore = storeMemberRepository
                .existsByStoreIdAndUserIdAndDeletedAtIsNull(storeId, memberUserId);

        if (!memberInStore) {
            throw new CustomException(ErrorCode.STORE_MEMBER_ACCESS_DENIED);
        }
    }

    private void validateFile(MultipartFile file) {
        // 파일 크기 제한: 10MB
        long maxBytes = 10L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }

        // 허용 MIME
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean allowed = ct.equals("application/pdf")
                || ct.equals("image/png")
                || ct.equals("image/jpg")
                || ct.equals("image/jpeg");

        if (!allowed) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}