package com.example.shiftmate.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

// 저장소 추상화 계층
// - 지금은 Local 구현 사용
// - 나중에 S3 구현으로 교체할 때 서비스 코드 변경 최소화
public interface FileStorageService {

    // 파일 저장
    // userId/typeFolder를 받아 저장 경로를 규칙적으로 구성한다.
    StoredFile save(Long userId, String typeFolder, MultipartFile file);

    // 파일 삭제
    // 재업로드 시 기존 파일 정리, 문서 삭제 API에서 사용
    void delete(String storedPath);

    // 파일 다운로드
    // storedPath(로컬 경로 또는 S3 key) 기준으로 원본 파일 바이트를 읽어 반환한다.
    // originalFileName/contentType은 다운로드 헤더를 구성하기 위한 메타 정보다.
    DownloadedFile download(String storedPath, String originalFileName, String contentType);

    // 저장 후 반환값 묶음
    // - storedPath: 실제 파일 시스템 경로
    // - fileUrl: 클라이언트 접근용 URL
    record StoredFile(String storedPath, String fileUrl) {}

    // 다운로드 반환값 묶음
    // - bytes: 실제 파일 바이트
    // - fileName: 다운로드 시 사용자에게 보여줄 파일명
    // - contentType: 응답 Content-Type
    record DownloadedFile(byte[] bytes, String fileName, String contentType) {}
}