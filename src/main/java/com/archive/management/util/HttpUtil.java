package com.archive.management.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP工具类
 * 提供HTTP请求和响应处理的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class HttpUtil {

    /**
     * 常用User-Agent
     */
    public static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    public static final String USER_AGENT_FIREFOX = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0";
    public static final String USER_AGENT_SAFARI = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15";

    /**
     * 常用Content-Type
     */
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_HTML = "text/html";

    // ========== 请求信息获取 ==========

    /**
     * 获取当前HTTP请求
     * 
     * @return HttpServletRequest对象
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前HTTP响应
     * 
     * @return HttpServletResponse对象
     */
    public static HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtil.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getRemoteAddr();
        return StringUtil.isNotEmpty(ip) ? ip.trim() : "unknown";
    }

    /**
     * 获取客户端IP地址（当前请求）
     * 
     * @return IP地址
     */
    public static String getClientIp() {
        return getClientIp(getCurrentRequest());
    }

    /**
     * 获取User-Agent
     * 
     * @param request HTTP请求
     * @return User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "";
    }

    /**
     * 获取User-Agent（当前请求）
     * 
     * @return User-Agent
     */
    public static String getUserAgent() {
        return getUserAgent(getCurrentRequest());
    }

    /**
     * 获取请求URL
     * 
     * @param request HTTP请求
     * @return 完整的请求URL
     */
    public static String getRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (StringUtil.isNotEmpty(queryString)) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }

    /**
     * 获取请求URL（当前请求）
     * 
     * @return 完整的请求URL
     */
    public static String getRequestUrl() {
        return getRequestUrl(getCurrentRequest());
    }

    /**
     * 获取Referer
     * 
     * @param request HTTP请求
     * @return Referer
     */
    public static String getReferer(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "";
    }

    /**
     * 获取Referer（当前请求）
     * 
     * @return Referer
     */
    public static String getReferer() {
        return getReferer(getCurrentRequest());
    }

    // ========== 请求头处理 ==========

    /**
     * 获取所有请求头
     * 
     * @param request HTTP请求
     * @return 请求头映射
     */
    public static Map<String, String> getAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        if (request == null) {
            return headers;
        }
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }

    /**
     * 获取所有请求头（当前请求）
     * 
     * @return 请求头映射
     */
    public static Map<String, String> getAllHeaders() {
        return getAllHeaders(getCurrentRequest());
    }

    /**
     * 获取请求头值
     * 
     * @param request HTTP请求
     * @param headerName 请求头名称
     * @return 请求头值
     */
    public static String getHeader(HttpServletRequest request, String headerName) {
        if (request == null || StringUtil.isEmpty(headerName)) {
            return null;
        }
        
        return request.getHeader(headerName);
    }

    /**
     * 获取请求头值（当前请求）
     * 
     * @param headerName 请求头名称
     * @return 请求头值
     */
    public static String getHeader(String headerName) {
        return getHeader(getCurrentRequest(), headerName);
    }

    // ========== 请求参数处理 ==========

    /**
     * 获取所有请求参数
     * 
     * @param request HTTP请求
     * @return 参数映射
     */
    public static Map<String, String> getAllParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        
        if (request == null) {
            return parameters;
        }
        
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = request.getParameter(parameterName);
            parameters.put(parameterName, parameterValue);
        }
        
        return parameters;
    }

    /**
     * 获取所有请求参数（当前请求）
     * 
     * @return 参数映射
     */
    public static Map<String, String> getAllParameters() {
        return getAllParameters(getCurrentRequest());
    }

    /**
     * 获取请求参数值
     * 
     * @param request HTTP请求
     * @param parameterName 参数名称
     * @return 参数值
     */
    public static String getParameter(HttpServletRequest request, String parameterName) {
        if (request == null || StringUtil.isEmpty(parameterName)) {
            return null;
        }
        
        return request.getParameter(parameterName);
    }

    /**
     * 获取请求参数值（当前请求）
     * 
     * @param parameterName 参数名称
     * @return 参数值
     */
    public static String getParameter(String parameterName) {
        return getParameter(getCurrentRequest(), parameterName);
    }

    /**
     * 获取请求参数值（带默认值）
     * 
     * @param request HTTP请求
     * @param parameterName 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        String value = getParameter(request, parameterName);
        return StringUtil.isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * 获取请求参数值（带默认值，当前请求）
     * 
     * @param parameterName 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(String parameterName, String defaultValue) {
        return getParameter(getCurrentRequest(), parameterName, defaultValue);
    }

    // ========== 响应处理 ==========

    /**
     * 设置响应头
     * 
     * @param response HTTP响应
     * @param name 头名称
     * @param value 头值
     */
    public static void setHeader(HttpServletResponse response, String name, String value) {
        if (response != null && StringUtil.isNotEmpty(name)) {
            response.setHeader(name, value);
        }
    }

    /**
     * 设置响应头（当前响应）
     * 
     * @param name 头名称
     * @param value 头值
     */
    public static void setHeader(String name, String value) {
        setHeader(getCurrentResponse(), name, value);
    }

    /**
     * 添加响应头
     * 
     * @param response HTTP响应
     * @param name 头名称
     * @param value 头值
     */
    public static void addHeader(HttpServletResponse response, String name, String value) {
        if (response != null && StringUtil.isNotEmpty(name)) {
            response.addHeader(name, value);
        }
    }

    /**
     * 添加响应头（当前响应）
     * 
     * @param name 头名称
     * @param value 头值
     */
    public static void addHeader(String name, String value) {
        addHeader(getCurrentResponse(), name, value);
    }

    /**
     * 设置Content-Type
     * 
     * @param response HTTP响应
     * @param contentType Content-Type
     */
    public static void setContentType(HttpServletResponse response, String contentType) {
        if (response != null && StringUtil.isNotEmpty(contentType)) {
            response.setContentType(contentType);
        }
    }

    /**
     * 设置Content-Type（当前响应）
     * 
     * @param contentType Content-Type
     */
    public static void setContentType(String contentType) {
        setContentType(getCurrentResponse(), contentType);
    }

    /**
     * 设置字符编码
     * 
     * @param response HTTP响应
     * @param encoding 字符编码
     */
    public static void setCharacterEncoding(HttpServletResponse response, String encoding) {
        if (response != null && StringUtil.isNotEmpty(encoding)) {
            response.setCharacterEncoding(encoding);
        }
    }

    /**
     * 设置字符编码（当前响应）
     * 
     * @param encoding 字符编码
     */
    public static void setCharacterEncoding(String encoding) {
        setCharacterEncoding(getCurrentResponse(), encoding);
    }

    // ========== 文件下载 ==========

    /**
     * 设置文件下载响应头
     * 
     * @param response HTTP响应
     * @param fileName 文件名
     */
    public static void setDownloadHeaders(HttpServletResponse response, String fileName) {
        if (response == null || StringUtil.isEmpty(fileName)) {
            return;
        }
        
        try {
            // 设置Content-Type
            response.setContentType("application/octet-stream");
            
            // 设置字符编码
            response.setCharacterEncoding("UTF-8");
            
            // 编码文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            
            // 设置Content-Disposition
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            // 设置缓存控制
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
        } catch (Exception e) {
            // 编码失败时使用原文件名
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        }
    }

    /**
     * 设置文件下载响应头（当前响应）
     * 
     * @param fileName 文件名
     */
    public static void setDownloadHeaders(String fileName) {
        setDownloadHeaders(getCurrentResponse(), fileName);
    }

    /**
     * 输出文件到响应
     * 
     * @param response HTTP响应
     * @param file 文件
     * @param fileName 下载文件名
     * @throws IOException IO异常
     */
    public static void outputFile(HttpServletResponse response, File file, String fileName) throws IOException {
        if (response == null || file == null || !file.exists()) {
            return;
        }
        
        // 设置下载头
        setDownloadHeaders(response, StringUtil.isNotEmpty(fileName) ? fileName : file.getName());
        
        // 设置文件大小
        response.setContentLengthLong(file.length());
        
        // 输出文件内容
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
        }
    }

    /**
     * 输出文件到响应（当前响应）
     * 
     * @param file 文件
     * @param fileName 下载文件名
     * @throws IOException IO异常
     */
    public static void outputFile(File file, String fileName) throws IOException {
        outputFile(getCurrentResponse(), file, fileName);
    }

    /**
     * 输出字节数组到响应
     * 
     * @param response HTTP响应
     * @param data 字节数组
     * @param fileName 下载文件名
     * @throws IOException IO异常
     */
    public static void outputBytes(HttpServletResponse response, byte[] data, String fileName) throws IOException {
        if (response == null || data == null) {
            return;
        }
        
        // 设置下载头
        setDownloadHeaders(response, fileName);
        
        // 设置文件大小
        response.setContentLength(data.length);
        
        // 输出数据
        try (OutputStream os = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * 输出字节数组到响应（当前响应）
     * 
     * @param data 字节数组
     * @param fileName 下载文件名
     * @throws IOException IO异常
     */
    public static void outputBytes(byte[] data, String fileName) throws IOException {
        outputBytes(getCurrentResponse(), data, fileName);
    }

    // ========== JSON响应 ==========

    /**
     * 输出JSON响应
     * 
     * @param response HTTP响应
     * @param data 数据对象
     * @throws IOException IO异常
     */
    public static void outputJson(HttpServletResponse response, Object data) throws IOException {
        if (response == null) {
            return;
        }
        
        // 设置响应头
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        
        // 转换为JSON
        String json = JsonUtil.toJson(data);
        
        // 输出JSON
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
            writer.flush();
        }
    }

    /**
     * 输出JSON响应（当前响应）
     * 
     * @param data 数据对象
     * @throws IOException IO异常
     */
    public static void outputJson(Object data) throws IOException {
        outputJson(getCurrentResponse(), data);
    }

    /**
     * 输出成功JSON响应
     * 
     * @param response HTTP响应
     * @param data 数据对象
     * @throws IOException IO异常
     */
    public static void outputSuccessJson(HttpServletResponse response, Object data) throws IOException {
        outputJson(response, ResponseUtil.success(data));
    }

    /**
     * 输出成功JSON响应（当前响应）
     * 
     * @param data 数据对象
     * @throws IOException IO异常
     */
    public static void outputSuccessJson(Object data) throws IOException {
        outputSuccessJson(getCurrentResponse(), data);
    }

    /**
     * 输出错误JSON响应
     * 
     * @param response HTTP响应
     * @param message 错误消息
     * @throws IOException IO异常
     */
    public static void outputErrorJson(HttpServletResponse response, String message) throws IOException {
        if (response != null) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        outputJson(response, ResponseUtil.error(message));
    }

    /**
     * 输出错误JSON响应（当前响应）
     * 
     * @param message 错误消息
     * @throws IOException IO异常
     */
    public static void outputErrorJson(String message) throws IOException {
        outputErrorJson(getCurrentResponse(), message);
    }

    // ========== 请求类型判断 ==========

    /**
     * 判断是否是Ajax请求
     * 
     * @param request HTTP请求
     * @return 是否是Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 判断是否是Ajax请求（当前请求）
     * 
     * @return 是否是Ajax请求
     */
    public static boolean isAjaxRequest() {
        return isAjaxRequest(getCurrentRequest());
    }

    /**
     * 判断是否是JSON请求
     * 
     * @param request HTTP请求
     * @return 是否是JSON请求
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        String contentType = request.getContentType();
        return StringUtil.isNotEmpty(contentType) && contentType.toLowerCase().contains("application/json");
    }

    /**
     * 判断是否是JSON请求（当前请求）
     * 
     * @return 是否是JSON请求
     */
    public static boolean isJsonRequest() {
        return isJsonRequest(getCurrentRequest());
    }

    /**
     * 判断是否是移动端请求
     * 
     * @param request HTTP请求
     * @return 是否是移动端请求
     */
    public static boolean isMobileRequest(HttpServletRequest request) {
        String userAgent = getUserAgent(request).toLowerCase();
        
        return userAgent.contains("mobile") ||
               userAgent.contains("android") ||
               userAgent.contains("iphone") ||
               userAgent.contains("ipad") ||
               userAgent.contains("windows phone") ||
               userAgent.contains("blackberry");
    }

    /**
     * 判断是否是移动端请求（当前请求）
     * 
     * @return 是否是移动端请求
     */
    public static boolean isMobileRequest() {
        return isMobileRequest(getCurrentRequest());
    }

    // ========== URL处理 ==========

    /**
     * 构建查询字符串
     * 
     * @param parameters 参数映射
     * @return 查询字符串
     */
    public static String buildQueryString(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        
        StringBuilder queryString = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (StringUtil.isEmpty(key) || value == null) {
                continue;
            }
            
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            
            try {
                queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()))
                          .append("=")
                          .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.name()));
            } catch (Exception e) {
                // 编码失败时跳过该参数
            }
        }
        
        return queryString.toString();
    }

    /**
     * 在URL后添加参数
     * 
     * @param url 原URL
     * @param parameters 参数映射
     * @return 带参数的URL
     */
    public static String appendParameters(String url, Map<String, Object> parameters) {
        if (StringUtil.isEmpty(url)) {
            return "";
        }
        
        String queryString = buildQueryString(parameters);
        if (StringUtil.isEmpty(queryString)) {
            return url;
        }
        
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + queryString;
    }
}