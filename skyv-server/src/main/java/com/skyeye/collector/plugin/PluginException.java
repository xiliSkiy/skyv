package com.skyeye.collector.plugin;

/**
 * 插件异常类
 * 
 * @author SkyEye Team
 */
public class PluginException extends Exception {

    private String errorCode;
    private String pluginType;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PluginException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public PluginException(String errorCode, String message, String pluginType) {
        super(message);
        this.errorCode = errorCode;
        this.pluginType = pluginType;
    }

    public PluginException(String errorCode, String message, String pluginType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.pluginType = pluginType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }
}
