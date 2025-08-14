package com.skyeye.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JsonUtils {

    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    /**
     * 对象转JSON字符串
     * 
     * @param object 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", object, e);
            return null;
        }
    }

    /**
     * 对象转格式化的JSON字符串
     * 
     * @param object 要转换的对象
     * @return 格式化的JSON字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转格式化JSON失败: {}", object, e);
            return null;
        }
    }

    /**
     * JSON字符串转对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("JSON转对象失败: json={}, class={}", json, clazz.getName(), e);
            return null;
        }
    }

    /**
     * JSON字符串转对象（使用TypeReference）
     * 
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("JSON转对象失败: json={}, typeReference={}", json, typeReference.getType(), e);
            return null;
        }
    }

    /**
     * JSON字符串转List
     * 
     * @param json JSON字符串
     * @param elementClass List元素类型
     * @param <T> 泛型类型
     * @return List对象
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementClass);
            return objectMapper.readValue(json, listType);
        } catch (IOException e) {
            log.error("JSON转List失败: json={}, elementClass={}", json, elementClass.getName(), e);
            return null;
        }
    }

    /**
     * JSON字符串转Map
     * 
     * @param json JSON字符串
     * @param keyClass Map键类型
     * @param valueClass Map值类型
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     * @return Map对象
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            MapType mapType = objectMapper.getTypeFactory()
                .constructMapType(Map.class, keyClass, valueClass);
            return objectMapper.readValue(json, mapType);
        } catch (IOException e) {
            log.error("JSON转Map失败: json={}, keyClass={}, valueClass={}", 
                json, keyClass.getName(), valueClass.getName(), e);
            return null;
        }
    }

    /**
     * 对象转Map
     * 
     * @param object 要转换的对象
     * @return Map对象
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("对象转Map失败: {}", object, e);
            return null;
        }
    }

    /**
     * Map转对象
     * 
     * @param map Map对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            log.error("Map转对象失败: map={}, class={}", map, clazz.getName(), e);
            return null;
        }
    }

    /**
     * 判断字符串是否为有效的JSON格式
     * 
     * @param json JSON字符串
     * @return true如果是有效的JSON格式
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 深度克隆对象（通过JSON序列化实现）
     * 
     * @param object 要克隆的对象
     * @param clazz 对象类型
     * @param <T> 泛型类型
     * @return 克隆后的对象
     */
    public static <T> T deepClone(T object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        
        String json = toJson(object);
        return fromJson(json, clazz);
    }
} 