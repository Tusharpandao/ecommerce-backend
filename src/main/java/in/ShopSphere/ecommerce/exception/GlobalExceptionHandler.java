package in.ShopSphere.ecommerce.exception;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Business exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "BUSINESS_ERROR")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.error("Validation exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "VALIDATION_ERROR")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", "AUTHENTICATION_ERROR")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password", "INVALID_CREDENTIALS")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "ACCESS_DENIED")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation errors: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>error("Validation failed", "VALIDATION_ERROR", 
                        "Multiple validation errors occurred");
        response.setData(errors);
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.error("HTTP message not readable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid request body", "INVALID_REQUEST_BODY")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.error("Method argument type mismatch: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid parameter type", "INVALID_PARAMETER_TYPE")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Data integrity violation", "DATA_INTEGRITY_ERROR")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR")
                        .withPath(getRequestPath(request)));
    }
    
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ApiResponse> handleClassCastException(ClassCastException e, HttpServletRequest request) {
        log.error("ClassCastException occurred: {}", e.getMessage(), e);
        
        String message = "Data type conversion error occurred. This might be due to cache serialization issues.";
        if (e.getMessage() != null && e.getMessage().contains("LinkedHashMap")) {
            message = "Cache data format error detected. Please try again or contact support if the issue persists.";
        }
        
        ApiResponse response = ApiResponse.builder()
            .success(false)
            .message(message)
            .error(new ApiResponse.ErrorInfo("DATA_TYPE_ERROR", message, message, message, message))
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private String getRequestPath(WebRequest request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRequestURI();
        }
        return "unknown";
    }
}
