package com.archive.management.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 提供JSON序列化和反序列化的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class JsonUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** ObjectMapper实例 */
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 创建ObjectMapper实例
     * 
     * @return ObjectMapper实例
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 配置序列化选项
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        
        // 配置反序列化选项
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        
        return mapper;
    }

    /**
     * 获取ObjectMapper实例
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ========== 序列化方法 ==========

    /**
     * 将对象转换为JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串，转换失败返回null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将对象转换为格式化的JSON字符串
     * 
     * @param obj 对象
     * @return 格式化的JSON字符串，转换失败返回null
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将对象转换为JSON字节数组
     * 
     * @param obj 对象
     * @return JSON字节数组，转换失败返回null
     */
    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // ========== 反序列化方法 ==========

    /**
     * 将JSON字符串转换为指定类型的对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象（使用TypeReference）
     * 
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtil.isEmpty(json) || typeReference == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将JSON字节数组转换为指定类型的对象
     * 
     * @param jsonBytes JSON字节数组
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T fromJsonBytes(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0 || clazz == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将JSON字节数组转换为指定类型的对象（使用TypeReference）
     * 
     * @param jsonBytes JSON字节数组
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T fromJsonBytes(byte[] jsonBytes, TypeReference<T> typeReference) {
        if (jsonBytes == null || jsonBytes.length == 0 || typeReference == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, typeReference);
        } catch (IOException e) {
            return null;
        }
    }

    // ========== 便捷方法 ==========

    /**
     * 将JSON字符串转换为Map
     * 
     * @param json JSON字符串
     * @return Map对象，转换失败返回null
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将JSON字符串转换为List
     * 
     * @param json JSON字符串
     * @param elementClass 列表元素类型
     * @param <T> 泛型类型
     * @return List对象，转换失败返回null
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (StringUtil.isEmpty(json) || elementClass == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, 
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将Map转换为指定类型的对象
     * 
     * @param map Map对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || clazz == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将对象转换为Map
     * 
     * @param obj 对象
     * @return Map对象，转换失败返回null
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ========== JSON节点操作 ==========

    /**
     * 解析JSON字符串为JsonNode
     * 
     * @param json JSON字符串
     * @return JsonNode对象，解析失败返回null
     */
    public static JsonNode parseJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 从JsonNode获取字符串值
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @return 字符串值，获取失败返回null
     */
    public static String getString(JsonNode node, String fieldName) {
        if (node == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * 从JsonNode获取字符串值（带默认值）
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    public static String getString(JsonNode node, String fieldName, String defaultValue) {
        String value = getString(node, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * 从JsonNode获取整数值
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @return 整数值，获取失败返回null
     */
    public static Integer getInteger(JsonNode node, String fieldName) {
        if (node == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.isNumber() ? fieldNode.asInt() : null;
    }

    /**
     * 从JsonNode获取整数值（带默认值）
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 整数值
     */
    public static Integer getInteger(JsonNode node, String fieldName, Integer defaultValue) {
        Integer value = getInteger(node, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * 从JsonNode获取长整数值
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @return 长整数值，获取失败返回null
     */
    public static Long getLong(JsonNode node, String fieldName) {
        if (node == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.isNumber() ? fieldNode.asLong() : null;
    }

    /**
     * 从JsonNode获取长整数值（带默认值）
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 长整数值
     */
    public static Long getLong(JsonNode node, String fieldName, Long defaultValue) {
        Long value = getLong(node, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * 从JsonNode获取布尔值
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @return 布尔值，获取失败返回null
     */
    public static Boolean getBoolean(JsonNode node, String fieldName) {
        if (node == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.isBoolean() ? fieldNode.asBoolean() : null;
    }

    /**
     * 从JsonNode获取布尔值（带默认值）
     * 
     * @param node JsonNode对象
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public static Boolean getBoolean(JsonNode node, String fieldName, Boolean defaultValue) {
        Boolean value = getBoolean(node, fieldName);
        return value != null ? value : defaultValue;
    }

    // ========== 验证方法 ==========

    /**
     * 验证字符串是否为有效的JSON
     * 
     * @param json JSON字符串
     * @return 是否为有效JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为有效的JSON对象
     * 
     * @param json JSON字符串
     * @return 是否为有效JSON对象
     */
    public static boolean isValidJsonObject(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node.isObject();
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为有效的JSON数组
     * 
     * @param json JSON字符串
     * @return 是否为有效JSON数组
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node.isArray();
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    // ========== 深拷贝方法 ==========

    /**
     * 深拷贝对象（通过JSON序列化和反序列化）
     * 
     * @param obj 原对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 深拷贝后的对象，拷贝失败返回null
     */
    public static <T> T deepCopy(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 深拷贝对象（通过JSON序列化和反序列化，使用TypeReference）
     * 
     * @param obj 原对象
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 深拷贝后的对象，拷贝失败返回null
     */
    public static <T> T deepCopy(Object obj, TypeReference<T> typeReference) {
        if (obj == null || typeReference == null) {
            return null;
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // ========== 合并方法 ==========

    /**
     * 合并两个JSON对象
     * 
     * @param json1 第一个JSON字符串
     * @param json2 第二个JSON字符串
     * @return 合并后的JSON字符串，合并失败返回null
     */
    public static String mergeJson(String json1, String json2) {
        if (StringUtil.isEmpty(json1)) {
            return json2;
        }
        if (StringUtil.isEmpty(json2)) {
            return json1;
        }
        
        try {
            JsonNode node1 = OBJECT_MAPPER.readTree(json1);
            JsonNode node2 = OBJECT_MAPPER.readTree(json2);
            
            if (!node1.isObject() || !node2.isObject()) {
                return null;
            }
            
            JsonNode merged = mergeJsonNodes(node1, node2);
            return OBJECT_MAPPER.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 合并两个JsonNode
     * 
     * @param node1 第一个JsonNode
     * @param node2 第二个JsonNode
     * @return 合并后的JsonNode
     */
    private static JsonNode mergeJsonNodes(JsonNode node1, JsonNode node2) {
        if (!node1.isObject() || !node2.isObject()) {
            return node2;
        }
        
        Map<String, Object> map1 = OBJECT_MAPPER.convertValue(node1, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> map2 = OBJECT_MAPPER.convertValue(node2, new TypeReference<Map<String, Object>>() {});
        
        map1.putAll(map2);
        
        return OBJECT_MAPPER.valueToTree(map1);
    }
}