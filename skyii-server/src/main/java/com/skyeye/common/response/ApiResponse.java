package com.skyeye.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * API统一响应格式
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 元数据，如分页信息等
     */
    private Object meta;

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null, "操作成功");
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 成功响应（带元数据）
     */
    public static <T> ApiResponse<T> success(T data, String message, Object meta) {
        ApiResponse<T> response = success(data, message);
        response.setMeta(meta);
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message);
    }

    /**
     * 失败响应（带状态码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
} 