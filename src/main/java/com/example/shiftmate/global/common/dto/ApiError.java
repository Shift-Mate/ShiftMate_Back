package com.example.shiftmate.global.common.dto;


import com.example.shiftmate.global.exception.ErrorCode;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiError {

    private final String code;

    private final String message;

    private final List<?> details;

    public static ApiError of(ErrorCode errorCode) {
        return new ApiError(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ApiError of(ErrorCode errorCode, List<?> details) {
        return new ApiError(errorCode.name(), errorCode.getMessage(), details);
    }


}
