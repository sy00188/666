package com.archive.management.common;

/**
 * 响应结果类（Result的别名）
 * 为了兼容性保留此类
 */
public class ResponseResult<T> extends Result<T> {
    
    public ResponseResult() {
        super();
    }
    
    public ResponseResult(Integer code, String message, T data) {
        super(code, message, data);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseResult<T> success() {
        return (ResponseResult<T>) Result.success();
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应（带状态码）
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}


