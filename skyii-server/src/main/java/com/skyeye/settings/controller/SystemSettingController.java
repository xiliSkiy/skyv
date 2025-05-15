package com.skyeye.settings.controller;

import com.skyeye.common.dto.SystemSettingDTO;
import com.skyeye.common.response.ApiResponse;
import com.skyeye.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 系统设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SystemSettingController {
    
    private final SystemSettingService systemSettingService;
    
    /**
     * 获取所有系统设置
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingDTO>>> getAllSettings() {
        List<SystemSettingDTO> settings = systemSettingService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
    
    /**
     * 根据设置键获取设置
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemSettingDTO>> getSetting(@PathVariable String key) {
        SystemSettingDTO setting = systemSettingService.getSetting(key);
        return ResponseEntity.ok(ApiResponse.success(setting));
    }
    
    /**
     * 根据设置组获取设置
     */
    @GetMapping("/group/{group}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettingsByGroup(@PathVariable String group) {
        Map<String, Object> settings = systemSettingService.getSettingsByGroup(group);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
    
    /**
     * 保存或更新设置
     */
    @PutMapping
    public ResponseEntity<ApiResponse<SystemSettingDTO>> saveSetting(@RequestBody SystemSettingDTO dto) {
        SystemSettingDTO savedSetting = systemSettingService.saveSetting(dto);
        return ResponseEntity.ok(ApiResponse.success(savedSetting));
    }
    
    /**
     * 批量保存或更新设置
     */
    @PutMapping("/batch")
    public ResponseEntity<ApiResponse<Boolean>> batchSaveSettings(@RequestBody Map<String, Map<String, Object>> settings) {
        boolean result = systemSettingService.batchSaveSettings(settings);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 删除设置
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable String key) {
        systemSettingService.deleteSetting(key);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/system-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemInfo() {
        Map<String, Object> info = systemSettingService.getSystemInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }
    
    /**
     * 上传系统Logo
     */
    @PostMapping("/upload/logo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadLogo(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "logo");
    }
    
    /**
     * 上传系统Favicon
     */
    @PostMapping("/upload/favicon")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFavicon(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "favicon");
    }
    
    /**
     * 重置为默认Logo
     */
    @PostMapping("/reset-logo")
    public ResponseEntity<ApiResponse<Void>> resetDefaultLogo() {
        // 删除自定义Logo设置
        systemSettingService.deleteSetting("basic.logoUrl");
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    /**
     * 获取操作日志
     */
    @GetMapping("/operation-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOperationLogs(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        // 这里应该从日志服务获取操作日志，这里简化处理
        List<Map<String, Object>> logs = List.of(
            Map.of(
                "id", 1,
                "title", "修改系统名称",
                "time", "今天 10:23",
                "user", "管理员 (admin)"
            ),
            Map.of(
                "id", 2,
                "title", "更新系统Logo",
                "time", "昨天 14:05",
                "user", "管理员 (admin)"
            ),
            Map.of(
                "id", 3,
                "title", "修改时区设置",
                "time", "2023-11-18",
                "user", "系统管理员 (sysadmin)"
            ),
            Map.of(
                "id", 4,
                "title", "系统升级至v2.5.3",
                "time", "2023-11-15",
                "user", "系统管理员 (sysadmin)"
            )
        );
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    /**
     * 检查系统更新
     */
    @GetMapping("/check-update")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSystemUpdate() {
        // 这里应该调用更新服务检查更新，这里简化处理
        Map<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("hasUpdate", false);
        updateInfo.put("currentVersion", "v2.5.3");
        updateInfo.put("latestVersion", "v2.5.3");
        updateInfo.put("releaseDate", "2023-11-15");
        updateInfo.put("releaseNotes", "当前已是最新版本");
        
        return ResponseEntity.ok(ApiResponse.success(updateInfo));
    }
    
    /**
     * 获取可用语言列表
     */
    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableLanguages() {
        // 返回系统支持的语言列表
        List<Map<String, Object>> languages = List.of(
            Map.of(
                "code", "zh_CN",
                "name", "简体中文",
                "flag", "/assets/flags/cn.png"
            ),
            Map.of(
                "code", "en_US",
                "name", "English",
                "flag", "/assets/flags/us.png"
            ),
            Map.of(
                "code", "ja_JP",
                "name", "日本語",
                "flag", "/assets/flags/jp.png"
            ),
            Map.of(
                "code", "ko_KR",
                "name", "한국어",
                "flag", "/assets/flags/kr.png"
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success(languages));
    }
    
    /**
     * 获取当前语言设置
     */
    @GetMapping("/current-language")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentLanguage() {
        // 从系统设置中获取当前语言设置
        Map<String, Object> languageSettings = systemSettingService.getSettingsByGroup("language");
        if (languageSettings.isEmpty()) {
            // 返回默认语言设置
            languageSettings = Map.of(
                "currentLanguage", "zh_CN",
                "defaultLanguage", "zh_CN",
                "enableMultiLanguage", true,
                "autoDetect", true,
                "numberFormat", "zh-CN",
                "currencySymbol", "CNY"
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success(languageSettings));
    }
    
    /**
     * 设置系统语言
     */
    @PostMapping("/set-language")
    public ResponseEntity<ApiResponse<Boolean>> setSystemLanguage(@RequestBody Map<String, String> languageData) {
        String language = languageData.get("language");
        if (language == null || language.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("语言代码不能为空"));
        }
        
        // 设置当前系统语言
        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setSettingKey("language.currentLanguage");
        dto.setSettingValue(language);
        dto.setSettingType("string");
        dto.setDescription("当前系统语言");
        systemSettingService.saveSetting(dto);
        
        return ResponseEntity.ok(ApiResponse.success(true));
    }
    
    /**
     * 获取许可证信息
     */
    @GetMapping("/license-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLicenseInfo() {
        // 从系统中获取许可证信息，这里简化处理
        Map<String, Object> licenseInfo = new HashMap<>();
        licenseInfo.put("licenseType", "企业版");
        licenseInfo.put("licenseKey", "SKYEYE-ENT-1234-5678-90AB-CDEF");
        licenseInfo.put("company", "智能安防科技有限公司");
        licenseInfo.put("contactPerson", "张三");
        licenseInfo.put("contactEmail", "zhangsan@example.com");
        licenseInfo.put("issueDate", "2023-01-01");
        licenseInfo.put("expiryDate", "2024-12-31");
        licenseInfo.put("maxDevices", 100);
        licenseInfo.put("currentDevices", 50);
        licenseInfo.put("features", Arrays.asList("基础监控", "智能分析", "AI识别", "多用户管理", "高级报表"));
        
        return ResponseEntity.ok(ApiResponse.success(licenseInfo));
    }
    
    /**
     * 上传许可证文件
     */
    @PostMapping("/upload/license")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadLicense(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请选择许可证文件"));
        }
        
        try {
            // 验证许可证文件，这里简化处理
            // 实际应用中应该解析许可证文件内容，验证签名等
            
            // 返回许可证信息
            Map<String, String> result = new HashMap<>();
            result.put("licenseKey", "SKYEYE-ENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("上传许可证文件失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("上传许可证文件失败: " + e.getMessage()));
        }
    }
    
    /**
     * 激活许可证
     */
    @PostMapping("/activate-license")
    public ResponseEntity<ApiResponse<Boolean>> activateLicense(@RequestBody Map<String, String> activationData) {
        String licenseKey = activationData.get("licenseKey");
        String activationCode = activationData.get("activationCode");
        
        if (licenseKey == null || licenseKey.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("许可证密钥不能为空"));
        }
        
        try {
            // 验证并激活许可证，这里简化处理
            // 实际应用中应该调用许可证服务进行验证和激活
            
            // 保存许可证信息到系统设置
            Map<String, Map<String, Object>> settings = new HashMap<>();
            Map<String, Object> licenseSettings = new HashMap<>();
            licenseSettings.put("licenseKey", licenseKey);
            licenseSettings.put("activationDate", new Date().toString());
            licenseSettings.put("status", "active");
            settings.put("license", licenseSettings);
            
            systemSettingService.batchSaveSettings(settings);
            
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            log.error("激活许可证失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("激活许可证失败: " + e.getMessage()));
        }
    }
    
    /**
     * 上传文件通用方法
     */
    private ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(MultipartFile file, String type) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请选择文件"));
        }
        
        try {
            // 文件存储路径（实际应用中应该配置到专门的存储服务或CDN）
            String uploadDir = "uploads/" + type;
            Path uploadPath = Paths.get(uploadDir);
            
            // 确保目录存在
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            String fileUrl = "/uploads/" + type + "/" + filename;
            
            // 更新系统设置
            SystemSettingDTO dto = new SystemSettingDTO();
            dto.setSettingKey("basic." + type + "Url");
            dto.setSettingValue(fileUrl);
            dto.setSettingType("string");
            dto.setDescription(type + "图片URL");
            systemSettingService.saveSetting(dto);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IOException e) {
            log.error("上传文件失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("上传文件失败: " + e.getMessage()));
        }
    }
}