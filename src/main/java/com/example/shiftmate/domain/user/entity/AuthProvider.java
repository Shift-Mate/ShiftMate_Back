package com.example.shiftmate.domain.user.entity;

// 로그인 방식 구분
public enum AuthProvider {
    LOCAL,   // 일반 이메일/비밀번호
    KAKAO,   // 카카오
    GOOGLE   // 구글
}