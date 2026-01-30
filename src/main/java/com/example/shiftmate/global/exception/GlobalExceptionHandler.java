package com.example.shiftmate.global.exception;



import com.example.shiftmate.global.common.dto.ApiError;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.common.dto.ValidationError;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        ApiError error = ApiError.of(e.getErrorCode());

        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
        MethodArgumentNotValidException e) {
        List<ValidationError> details = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
            .toList();

        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST, details);

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(
        MethodArgumentTypeMismatchException e) {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST,
            List.of(e.getName() + "잘못된 형식입니다."));

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e) {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST, List.of("요청 본문이 필요합니다."));

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(error));
    }

}
