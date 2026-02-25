package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${app.upload.s3.bucket}")
    private String bucket;

    @Value("${app.upload.s3.region}")
    private String region;

    @Value("${app.upload.s3.prefix:user-documents}")
    private String prefix;

    @Value("${app.upload.s3.public-base-url:}")
    private String publicBaseUrl;

    public S3FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public StoredFile save(Long userId, String typeFolder, MultipartFile file) {
        try {
            // 파일명이 비어있을 수 있으므로 기본값 보정
            String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                    ? "file"
                    : file.getOriginalFilename();

            // 공백/특수문자 문제를 줄이기 위해 안전한 파일명으로 인코딩
            String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                    .replace("+", "_");

            // 파일명 충돌 방지용 UUID
            String fileName = UUID.randomUUID() + "-" + encodedName;

            // S3 Object Key 규칙:
            // {prefix}/users/{userId}/{typeFolder}/{uuid}-{filename}
            String key = String.format(
                    "%s/users/%d/%s/%s",
                    trimSlashes(prefix),
                    userId,
                    typeFolder,
                    fileName
            );

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // MultipartFile 바이트를 S3에 업로드
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            // DB의 storedPath에는 "실제 저장 식별자"를 저장한다.
            // local은 파일 경로, s3는 object key를 저장하면 delete 시 재사용 가능하다.
            String storedPath = key;

            // 프론트 미리보기/열기용 URL 생성
            String fileUrl = buildPublicUrl(key);

            return new StoredFile(storedPath, fileUrl);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            // storedPath는 s3 object key로 취급
            if (storedPath == null || storedPath.isBlank()) {
                return;
            }

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storedPath)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception ignored) {
            // 삭제 실패는 현재 정책상 무시
        }
    }

    @Override
    public DownloadedFile download(String storedPath, String originalFileName, String contentType) {
        try {
            // 1) S3 key 유효성 체크
            //    storedPath에는 S3 key가 저장된다는 전제다.
            if (storedPath == null || storedPath.isBlank()) {
                throw new CustomException(ErrorCode.DOCUMENT_DOWNLOAD_FAILED);
            }

            // 2) S3에서 객체 바이트를 조회한다.
            //    권한 문제/키 없음/네트워크 오류 등은 catch에서 통합 처리한다.
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(storedPath)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            byte[] bytes = objectBytes.asByteArray();

            // 3) contentType 우선순위
            //    DB 값 -> S3 메타데이터 -> binary fallback
            String resolvedContentType = contentType;
            if (resolvedContentType == null || resolvedContentType.isBlank()) {
                resolvedContentType = objectBytes.response().contentType();
            }
            if (resolvedContentType == null || resolvedContentType.isBlank()) {
                resolvedContentType = "application/octet-stream";
            }

            // 4) 파일명 보정
            String fileName = (originalFileName == null || originalFileName.isBlank())
                    ? "document"
                    : originalFileName;

            // 5) 컨트롤러 파일 응답에 바로 쓰도록 반환
            return new DownloadedFile(bytes, fileName, resolvedContentType);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DOCUMENT_DOWNLOAD_FAILED);
        }
    }

    private String buildPublicUrl(String key) {
        // CloudFront/커스텀 도메인이 있으면 그 URL 기준으로 반환
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return trimRightSlash(publicBaseUrl) + "/" + key;
        }

        // 기본 S3 URL 형식
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    private String trimSlashes(String value) {
        if (value == null || value.isBlank()) return "";
        String result = value;
        while (result.startsWith("/")) result = result.substring(1);
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private String trimRightSlash(String value) {
        if (value == null) return "";
        String result = value;
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }
}