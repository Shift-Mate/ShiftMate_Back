package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.request.SignUpRequest;
import com.example.shiftmate.domain.auth.dto.response.SignUpResponse;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User saved = userRepository.save(user);
        return new SignUpResponse(saved);
    }


}
