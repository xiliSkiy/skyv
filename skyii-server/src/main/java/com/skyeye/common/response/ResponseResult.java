package com.skyeye.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * ResponseResult类，用于统一API响应格式
 */
@Data
public class ResponseResult<T> implements Serializable {

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
     * 成功响应
     */
    public static <T> ResponseResult<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseResult<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> ResponseResult<T> success(T data, String message) {
        ResponseResult<T> response = new ResponseResult<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 成功响应（带元数据）
     */
    public static <T> ResponseResult<T> success(T data, String message, Object meta) {
        ResponseResult<T> response = success(data, message);
        response.setMeta(meta);
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        return error(400, message);
    }

    /**
     * 失败响应（带状态码）
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        ResponseResult<T> response = new ResponseResult<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
} 