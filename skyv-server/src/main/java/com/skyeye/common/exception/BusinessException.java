package com.skyeye.common.exception;

import com.skyeye.common.response.ResponseCode;
import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /** 错误码 */
    private final Integer code;
    
    /** 错误消息 */
    private final String message;
    
    /** 错误数据 */
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
        this.data = null;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = null;
    }

    public BusinessException(String message, Object data) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
        this.data = data;
    }

    public BusinessException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BusinessException(ResponseCode responseCode, Object data) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
        this.data = null;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.data = null;
    }

    @Override
    public String toString() {
        return String.format("BusinessException{code=%d, message='%s', data=%s}", 
            code, message, data);
    }
} 