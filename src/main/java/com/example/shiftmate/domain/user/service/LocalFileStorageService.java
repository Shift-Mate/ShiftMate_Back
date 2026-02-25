package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "app.upload.storage-type",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.local-dir}")
    private String localDir;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    @Override
    public StoredFile save(Long userId, String typeFolder, MultipartFile file) {
        try {
            // 1) 파일명 정리
            // - 원본 이름이 비어있으면 기본값 사용
            // - 공백은 '_'로 치환해 경로/URL 안정성 확보
            String safeName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                    ? "file"
                    : file.getOriginalFilename().replaceAll("\\s+", "_");

            // 2) UUID를 파일명 앞에 붙여 동일 이름 충돌 방지
            String fileName = UUID.randomUUID() + "-" + safeName;

            // 3) 저장 디렉토리 규칙
            //    uploads/users/{userId}/{typeFolder}/
            Path targetDir = Paths.get(localDir, "users", String.valueOf(userId), typeFolder);

            // 4) 디렉토리 없으면 생성
            Files.createDirectories(targetDir);

            // 5) 실제 파일 복사 저장
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 6) DB 저장용 path와 응답용 URL 구성
            String storedPath = targetPath.toString();
            String fileUrl = baseUrl + "/users/" + userId + "/" + typeFolder + "/" + fileName;

            return new StoredFile(storedPath, fileUrl);
        } catch (IOException e) {
            // 저장 실패는 도메인 에러코드로 통일
            throw new CustomException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            // 경로가 비어있으면 아무것도 하지 않음
            if (storedPath == null || storedPath.isBlank()) {
                return;
            }

            // 파일이 실제로 있으면 삭제
            Files.deleteIfExists(Paths.get(storedPath));
        } catch (IOException ignored) {
            // 삭제 실패는 현재 정책상 무시
            // (필요하면 log.warn 추가 가능)
        }
    }

    @Override
    public DownloadedFile download(String storedPath, String originalFileName, String contentType) {
        try {
            // 1) 경로 유효성 체크
            //    DB에 경로가 비어 있으면 바로 도메인 에러로 반환한다.
            if (storedPath == null || storedPath.isBlank()) {
                throw new CustomException(ErrorCode.DOCUMENT_DOWNLOAD_FAILED);
            }

            Path path = Paths.get(storedPath);

            // 2) 파일 존재 여부 확인
            //    메타는 있는데 실제 파일이 없을 수 있으므로 방어한다.
            if (!Files.exists(path)) {
                throw new CustomException(ErrorCode.DOCUMENT_DOWNLOAD_FAILED);
            }

            // 3) 실제 파일 바이트를 메모리로 읽는다.
            byte[] bytes = Files.readAllBytes(path);

            // 4) 응답 contentType 보정
            //    - DB contentType을 우선 사용
            //    - 없으면 파일 시스템에서 추론
            //    - 그래도 없으면 안전한 binary 타입으로 fallback
            String resolvedContentType = contentType;
            if (resolvedContentType == null || resolvedContentType.isBlank()) {
                resolvedContentType = Files.probeContentType(path);
            }
            if (resolvedContentType == null || resolvedContentType.isBlank()) {
                resolvedContentType = "application/octet-stream";
            }

            // 5) 파일명 보정
            //    원본 파일명이 비어 있으면 기본 이름으로 내려준다.
            String fileName = (originalFileName == null || originalFileName.isBlank())
                    ? "document"
                    : originalFileName;

            // 6) 컨트롤러가 바로 응답으로 내릴 수 있도록 묶어서 반환한다.
            return new DownloadedFile(bytes, fileName, resolvedContentType);
        } catch (CustomException e) {
            // 도메인 에러는 그대로 전달한다.
            throw e;
        } catch (IOException e) {
            // 파일 읽기 실패는 다운로드 실패 코드로 통일한다.
            throw new CustomException(ErrorCode.DOCUMENT_DOWNLOAD_FAILED);
        }
    }
}