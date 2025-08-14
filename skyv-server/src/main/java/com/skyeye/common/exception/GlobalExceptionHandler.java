package com.skyeye.common.exception;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.common.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage(), e.getData());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 处理参数验证异常 - @Valid注解验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ResponseCode.VALIDATION_ERROR.getCode(), 
            "参数验证失败", 
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
            BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ResponseCode.VALIDATION_ERROR.getCode(), 
            "参数绑定失败", 
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常 - @Validated注解验证失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束验证失败 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ResponseCode.VALIDATION_ERROR.getCode(), 
            "约束验证失败", 
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配 - 请求路径: {}, 参数: {}, 值: {}", 
            request.getRequestURI(), e.getName(), e.getValue());
        
        String message = String.format("参数 '%s' 的值 '%s' 类型不正确", e.getName(), e.getValue());
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.BAD_REQUEST.getCode(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败 - 请求路径: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.UNAUTHORIZED.getCode(), "认证失败");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足 - 请求路径: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.FORBIDDEN.getCode(), "权限不足");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在 - 请求路径: {}", request.getRequestURI());
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.NOT_FOUND.getCode(), "请求路径不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - 请求路径: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.BAD_REQUEST.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态异常 - 请求路径: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.BUSINESS_ERROR.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - 请求路径: {}", request.getRequestURI(), e);
        
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.INTERNAL_ERROR.getCode(), "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 