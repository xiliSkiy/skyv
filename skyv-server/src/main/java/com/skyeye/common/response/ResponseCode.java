package com.skyeye.common.response;

/**
 * 响应码枚举
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum ResponseCode {
    
    // 成功响应 2xx
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    ACCEPTED(202, "请求已接受"),
    NO_CONTENT(204, "无内容"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    // 服务器错误 5xx
    INTERNAL_ERROR(500, "内部服务器错误"),
    NOT_IMPLEMENTED(501, "功能未实现"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    
    // 业务错误 6xx
    BUSINESS_ERROR(600, "业务处理失败"),
    DATA_NOT_FOUND(601, "数据不存在"),
    DATA_ALREADY_EXISTS(602, "数据已存在"),
    OPERATION_NOT_ALLOWED(603, "操作不被允许"),
    INSUFFICIENT_PERMISSIONS(604, "权限不足"),
    ACCOUNT_LOCKED(605, "账户已锁定"),
    ACCOUNT_EXPIRED(606, "账户已过期"),
    PASSWORD_EXPIRED(607, "密码已过期"),
    LOGIN_FAILED(608, "登录失败"),
    TOKEN_EXPIRED(609, "令牌已过期"),
    TOKEN_INVALID(610, "令牌无效"),
    
    // 设备相关错误 7xx
    DEVICE_NOT_FOUND(701, "设备不存在"),
    DEVICE_OFFLINE(702, "设备离线"),
    DEVICE_BUSY(703, "设备忙碌"),
    DEVICE_ERROR(704, "设备错误"),
    DEVICE_UNAUTHORIZED(705, "设备未授权"),
    DEVICE_TIMEOUT(706, "设备响应超时"),
    
    // 任务相关错误 8xx
    TASK_NOT_FOUND(801, "任务不存在"),
    TASK_RUNNING(802, "任务正在运行"),
    TASK_FAILED(803, "任务执行失败"),
    TASK_TIMEOUT(804, "任务执行超时"),
    TASK_CANCELLED(805, "任务已取消"),
    
    // 文件相关错误 9xx
    FILE_NOT_FOUND(901, "文件不存在"),
    FILE_TOO_LARGE(902, "文件过大"),
    FILE_TYPE_NOT_SUPPORTED(903, "文件类型不支持"),
    FILE_UPLOAD_FAILED(904, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(905, "文件下载失败");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据代码获取响应码枚举
     * 
     * @param code 响应码
     * @return 响应码枚举
     */
    public static ResponseCode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (ResponseCode responseCode : values()) {
            if (responseCode.code.equals(code)) {
                return responseCode;
            }
        }
        
        return null;
    }

    /**
     * 判断是否为成功响应码
     * 
     * @return true如果是成功响应码
     */
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * 判断是否为客户端错误响应码
     * 
     * @return true如果是客户端错误响应码
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否为服务器错误响应码
     * 
     * @return true如果是服务器错误响应码
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    /**
     * 判断是否为业务错误响应码
     * 
     * @return true如果是业务错误响应码
     */
    public boolean isBusinessError() {
        return code >= 600 && code < 1000;
    }

    @Override
    public String toString() {
        return String.format("ResponseCode{code=%d, message='%s'}", code, message);
    }
} 