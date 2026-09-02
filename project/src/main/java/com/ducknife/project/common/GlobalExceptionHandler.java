package com.ducknife.project.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ducknife.project.common.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler { // thêm vào để spring map 400/404/405 vì
                                                                             // cái này (resolver #3) chạy sau
                                                                             // @ExceptionHandler(Exception.class) (#1)

    // bắt lỗi nghiệp vụ
    @ExceptionHandler(AppException.class) // lấy nó và tất cả class con của nó
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }

    // Bắt lỗi duplicateKey -> Spring bắt từ DB, dùng cho JdbcTemplate
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("Trùng khóa khi ghi DB: {}", e.getMessage());
        return ApiResponse.error(409, "Dữ liệu đã tồn tại trong hệ thống");
    }

    // Bắt lỗi từ DB -> dùng cho Spring data jpa
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataViolation(DataIntegrityViolationException e) {
        log.warn("Vi phạm ràng buộc DB: {}", e.getMessage());
        return ApiResponse.error(409, "Dữ liệu đã tồn tại trong hệ thống");
    }

    // Bắt lỗi xác thực
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e) {
        String message = (e instanceof BadCredentialsException)
                ? "Tài khoản hoặc mật khẩu không chính xác"
                : "Xác thực không thành công";
        return ApiResponse.error(401, message);
    }

    // Bắt lỗi phân quyền
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponse.error(403, "Bạn không có quyền truy cập vào tài nguyên này");
    }

    // Bắt lỗi truyền type không đúng 
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResponse.error(400, "Tham số " + e.getName() + " không hợp lệ");
    }

    // Bắt lỗi hệ thống, lưới cuối cùng bắt những lỗi ko ngờ tới
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnwantedException(Exception e) {
        log.error("Lỗi không lường trước: {}", e.getMessage(), e);
        return ApiResponse.error(500, "Lỗi hệ thống, vui lòng thử lại sau!");
    }

    // Bắt lỗi validation
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

        e.getBindingResult().getGlobalErrors()
                .forEach(ge -> errors.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));

        return respond(e, "Dữ liệu không hợp lệ!", errors, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
        HandlerMethodValidationException e, HttpHeaders headers, 
        HttpStatusCode statusCode, WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>(); 

        e.getParameterValidationResults().forEach(
            result -> {
                String name = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(err -> 
                    errors.putIfAbsent(name != null ? name : "parameter", err.getDefaultMessage())
                );
            }
        );

        return respond(e, "Tham số không hợp lệ", errors, headers, HttpStatus.BAD_REQUEST, request);
    }

    // bọc mọi lỗi vào ApiResponse
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Lỗi server: [{}]: {}", statusCode.value(), e.getMessage(), e);
        } else {
            log.warn("Request lỗi [{}] {}: {}", statusCode.value(), e.getClass().getSimpleName(), e.getMessage());
        }

        Object payload = (body instanceof ApiResponse)
                ? body
                : ApiResponse.<Object>builder()
                        .status(statusCode.value())
                        .message(messageFor(statusCode))
                        .data(null)
                        .build();

        return super.handleExceptionInternal(e, payload, headers, statusCode, request);
    }

    // Helpers

    private ResponseEntity<Object> respond(Exception e, String message, Object data,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiResponse<Object> payload = ApiResponse.<Object>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build();

        return handleExceptionInternal(e, payload, headers, status, request);
    }

    private String messageFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Dữ liệu không hợp lệ";
            case 404 -> "Không tìm thấy dữ liệu";
            case 405 -> "Phương thức HTTP không hỗ trợ cho đường dẫn này";
            case 406 -> "Định dạng phản hồi không được chấp nhận";
            case 415 -> "Định dạng dữ liệu gửi lên không được hỗ trợ";
            default -> {
                HttpStatus resolved = HttpStatus.resolve(status.value());
                yield resolved != null ? resolved.getReasonPhrase() : "Yêu cầu không hợp lệ";
            }
        };
    }
}
