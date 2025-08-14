package com.skyeye.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应格式
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /** 响应码 */
    private Integer code;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;
    
    /** 元数据信息 */
    private Map<String, Object> meta;
    
    /** 时间戳 */
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }

    /**
     * 失败响应（使用预定义错误码）
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage());
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    /**
     * 参数验证失败响应
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return error(ResponseCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 业务异常响应
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return error(ResponseCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return error(ResponseCode.UNAUTHORIZED);
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden() {
        return error(ResponseCode.FORBIDDEN);
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound() {
        return error(ResponseCode.NOT_FOUND);
    }

    /**
     * 内部服务器错误响应
     */
    public static <T> ApiResponse<T> internalError() {
        return error(ResponseCode.INTERNAL_ERROR);
    }

    /**
     * 添加元数据
     */
    public ApiResponse<T> addMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new HashMap<>();
        }
        this.meta.put(key, value);
        return this;
    }

    /**
     * 添加分页元数据
     */
    public ApiResponse<T> addPageMeta(long total, int page, int size) {
        return addMeta("total", total)
               .addMeta("page", page)
               .addMeta("size", size)
               .addMeta("totalPages", (total + size - 1) / size);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
} 