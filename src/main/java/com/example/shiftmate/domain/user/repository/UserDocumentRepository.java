package com.example.shiftmate.domain.user.repository;

import com.example.shiftmate.domain.user.entity.UserDocument;
import com.example.shiftmate.domain.user.entity.UserDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    // 사용자 + 타입으로 단건 조회한다.
    // 업로드 시 "이미 있는 타입인지" 확인할 때 사용한다.
    Optional<UserDocument> findByUserIdAndType(Long userId, UserDocumentType type);

    // 내 문서 전체 목록 조회(보건증/신분증 등)
    List<UserDocument> findByUserId(Long userId);

    // 특정 타입 문서를 빠르게 삭제할 때 사용
    void deleteByUserIdAndType(Long userId, UserDocumentType type);
}