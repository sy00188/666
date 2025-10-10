package com.archive.management.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端工具类
 * 提供HTTP请求的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class HttpClientUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 默认超时时间（毫秒）
     */
    private static final int DEFAULT_TIMEOUT = 30000;
    
    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_COUNT = 3;
    
    /**
     * 默认重试间隔（毫秒）
     */
    private static final int DEFAULT_RETRY_INTERVAL = 1000;

    // ========== GET 请求 ==========

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @return 响应字符串
     */
    public static String get(String url) {
        return get(url, null, null, String.class);
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @return 响应字符串
     */
    public static String get(String url, Map<String, Object> params) {
        return get(url, params, null, String.class);
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) {
        return get(url, params, headers, String.class);
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T get(String url, Map<String, Object> params, 
                           Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建URL
            URI uri = buildUri(url, params);
            
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            
            // 创建请求实体
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("GET请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送GET请求并返回JSON对象
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @param typeReference 类型引用
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T getForObject(String url, Map<String, Object> params, 
                                    Map<String, String> headers, TypeReference<T> typeReference) {
        String jsonResponse = get(url, params, headers, String.class);
        return JsonUtil.fromJson(jsonResponse, typeReference);
    }

    // ========== POST 请求 ==========

    /**
     * 发送POST请求（JSON格式）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应字符串
     */
    public static String post(String url, Object body) {
        return post(url, body, null, String.class);
    }

    /**
     * 发送POST请求（JSON格式）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String post(String url, Object body, Map<String, String> headers) {
        return post(url, body, headers, String.class);
    }

    /**
     * 发送POST请求（JSON格式）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // 序列化请求体
            String jsonBody = body instanceof String ? (String) body : JsonUtil.toJson(body);
            
            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("POST请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送POST请求并返回JSON对象
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param typeReference 类型引用
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T postForObject(String url, Object body, Map<String, String> headers, 
                                     TypeReference<T> typeReference) {
        String jsonResponse = post(url, body, headers, String.class);
        return JsonUtil.fromJson(jsonResponse, typeReference);
    }

    /**
     * 发送POST表单请求
     * 
     * @param url 请求URL
     * @param formData 表单数据
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, Object> formData) {
        return postForm(url, formData, null, String.class);
    }

    /**
     * 发送POST表单请求
     * 
     * @param url 请求URL
     * @param formData 表单数据
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T postForm(String url, Map<String, Object> formData, 
                                Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 构建表单数据
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            if (formData != null) {
                formData.forEach((key, value) -> 
                    form.add(key, value != null ? value.toString() : ""));
            }
            
            // 创建请求实体
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("POST表单请求失败: " + e.getMessage(), e);
        }
    }

    // ========== PUT 请求 ==========

    /**
     * 发送PUT请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应字符串
     */
    public static String put(String url, Object body) {
        return put(url, body, null, String.class);
    }

    /**
     * 发送PUT请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // 序列化请求体
            String jsonBody = body instanceof String ? (String) body : JsonUtil.toJson(body);
            
            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("PUT请求失败: " + e.getMessage(), e);
        }
    }

    // ========== DELETE 请求 ==========

    /**
     * 发送DELETE请求
     * 
     * @param url 请求URL
     * @return 响应字符串
     */
    public static String delete(String url) {
        return delete(url, null, null, String.class);
    }

    /**
     * 发送DELETE请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T delete(String url, Map<String, Object> params, 
                              Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建URL
            URI uri = buildUri(url, params);
            
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            
            // 创建请求实体
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.DELETE, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("DELETE请求失败: " + e.getMessage(), e);
        }
    }

    // ========== PATCH 请求 ==========

    /**
     * 发送PATCH请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应字符串
     */
    public static String patch(String url, Object body) {
        return patch(url, body, null, String.class);
    }

    /**
     * 发送PATCH请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     */
    public static <T> T patch(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // 序列化请求体
            String jsonBody = body instanceof String ? (String) body : JsonUtil.toJson(body);
            
            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("PATCH请求失败: " + e.getMessage(), e);
        }
    }

    // ========== 异步请求 ==========

    /**
     * 异步GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return CompletableFuture
     */
    public static CompletableFuture<String> getAsync(String url, Map<String, Object> params, 
                                                    Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> get(url, params, headers));
    }

    /**
     * 异步POST请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return CompletableFuture
     */
    public static CompletableFuture<String> postAsync(String url, Object body, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> post(url, body, headers));
    }

    // ========== 带重试的请求 ==========

    /**
     * 带重试的GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @param retryCount 重试次数
     * @return 响应字符串
     */
    public static String getWithRetry(String url, Map<String, Object> params, 
                                     Map<String, String> headers, int retryCount) {
        return executeWithRetry(() -> get(url, params, headers), retryCount);
    }

    /**
     * 带重试的POST请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param retryCount 重试次数
     * @return 响应字符串
     */
    public static String postWithRetry(String url, Object body, Map<String, String> headers, int retryCount) {
        return executeWithRetry(() -> post(url, body, headers), retryCount);
    }

    /**
     * 执行带重试的操作
     * 
     * @param operation 操作
     * @param retryCount 重试次数
     * @param <T> 返回类型
     * @return 操作结果
     */
    private static <T> T executeWithRetry(java.util.function.Supplier<T> operation, int retryCount) {
        Exception lastException = null;
        
        for (int i = 0; i <= retryCount; i++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                
                if (i < retryCount) {
                    try {
                        Thread.sleep(DEFAULT_RETRY_INTERVAL);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("请求重试失败，重试次数: " + retryCount, lastException);
    }

    // ========== 批量请求 ==========

    /**
     * 批量GET请求
     * 
     * @param urls URL列表
     * @param headers 请求头
     * @return 响应列表
     */
    public static Map<String, String> batchGet(String[] urls, Map<String, String> headers) {
        Map<String, String> results = new HashMap<>();
        
        CompletableFuture<?>[] futures = new CompletableFuture[urls.length];
        
        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    String response = get(url, null, headers);
                    synchronized (results) {
                        results.put(url, response);
                    }
                    return response;
                } catch (Exception e) {
                    synchronized (results) {
                        results.put(url, "ERROR: " + e.getMessage());
                    }
                    return null;
                }
            });
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures).join();
        
        return results;
    }

    // ========== 文件上传 ==========

    /**
     * 上传文件
     * 
     * @param url 上传URL
     * @param fileBytes 文件字节数组
     * @param fileName 文件名
     * @param fieldName 字段名
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String uploadFile(String url, byte[] fileBytes, String fileName, 
                                   String fieldName, Map<String, String> headers) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 构建多部分表单数据
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(fieldName, new org.springframework.core.io.ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            
            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, httpHeaders);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    // ========== 下载文件 ==========

    /**
     * 下载文件
     * 
     * @param url 下载URL
     * @param headers 请求头
     * @return 文件字节数组
     */
    public static byte[] downloadFile(String url, Map<String, String> headers) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            
            // 创建请求实体
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            // 发送请求
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 构建URI
     * 
     * @param url 基础URL
     * @param params 参数
     * @return URI
     */
    private static URI buildUri(String url, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        
        if (params != null && !params.isEmpty()) {
            params.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value.toString());
                }
            });
        }
        
        return builder.build().encode(StandardCharsets.UTF_8).toUri();
    }

    /**
     * 构建请求头
     * 
     * @param headers 请求头映射
     * @return HttpHeaders
     */
    private static HttpHeaders buildHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        
        // 设置默认请求头
        httpHeaders.set("User-Agent", "HttpClientUtil/1.0");
        httpHeaders.set("Accept", "*/*");
        httpHeaders.set("Connection", "keep-alive");
        
        // 添加自定义请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(httpHeaders::set);
        }
        
        return httpHeaders;
    }

    /**
     * 构建认证头
     * 
     * @param token 认证令牌
     * @return 请求头映射
     */
    public static Map<String, String> buildAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        if (StringUtil.isNotEmpty(token)) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }

    /**
     * 构建基本认证头
     * 
     * @param username 用户名
     * @param password 密码
     * @return 请求头映射
     */
    public static Map<String, String> buildBasicAuthHeaders(String username, String password) {
        Map<String, String> headers = new HashMap<>();
        if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
            String auth = username + ":" + password;
            String encodedAuth = java.util.Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encodedAuth);
        }
        return headers;
    }

    /**
     * 检查URL是否有效
     * 
     * @param url URL字符串
     * @return 是否有效
     */
    public static boolean isValidUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return false;
        }
        
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取响应状态信息
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @return 状态信息
     */
    public static HttpStatus getResponseStatus(String url, Map<String, String> headers) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = buildHeaders(headers);
            
            // 创建请求实体
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            // 发送HEAD请求
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.HEAD, entity, Void.class);
            
            // Spring 6.0+ 返回HttpStatusCode，需要转换为HttpStatus
            return HttpStatus.valueOf(response.getStatusCode().value());
            
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * 测试连接
     * 
     * @param url 测试URL
     * @param timeout 超时时间（毫秒）
     * @return 是否连接成功
     */
    public static boolean testConnection(String url, int timeout) {
        try {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    HttpStatus status = getResponseStatus(url, null);
                    return status.is2xxSuccessful() || status.is3xxRedirection();
                } catch (Exception e) {
                    return false;
                }
            });
            
            return future.get(timeout, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            return false;
        }
    }
}