package com.example.shiftmate.domain.auth.dto.response;

import com.example.shiftmate.domain.user.entity.User;

public class SignUpResponse {
    private final Long id;
    private final String email;
    private final String name;

    public SignUpResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
    }
}
