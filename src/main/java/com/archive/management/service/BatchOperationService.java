package com.archive.management.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量操作服务接口
 * 提供智能分批处理、进度追踪、错误处理等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface BatchOperationService {
    
    /**
     * 执行批量操作
     * 
     * @param ids 要操作的ID列表
     * @param operation 操作函数（接收单个ID，返回操作结果）
     * @param progressCallback 进度回调
     * @param <T> ID类型
     * @return 批量操作结果
     */
    <T> BatchOperationResult executeBatchOperation(
        List<T> ids,
        Function<T, OperationResult> operation,
        Consumer<BatchProgress> progressCallback
    );
    
    /**
     * 执行批量操作（带上下文）
     * 
     * @param items 要操作的项目列表
     * @param operation 操作函数
     * @param progressCallback 进度回调
     * @param <T> 项目类型
     * @return 批量操作结果
     */
    <T> BatchOperationResult executeBatchOperationWithContext(
        List<T> items,
        Function<T, OperationResult> operation,
        Consumer<BatchProgress> progressCallback
    );
    
    /**
     * 单个操作结果
     */
    class OperationResult {
        private boolean success;
        private String message;
        private Object data;
        private String errorCode;
        
        public static OperationResult success() {
            OperationResult result = new OperationResult();
            result.success = true;
            return result;
        }
        
        public static OperationResult success(String message) {
            OperationResult result = new OperationResult();
            result.success = true;
            result.message = message;
            return result;
        }
        
        public static OperationResult success(String message, Object data) {
            OperationResult result = new OperationResult();
            result.success = true;
            result.message = message;
            result.data = data;
            return result;
        }
        
        public static OperationResult failure(String message) {
            OperationResult result = new OperationResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        public static OperationResult failure(String message, String errorCode) {
            OperationResult result = new OperationResult();
            result.success = false;
            result.message = message;
            result.errorCode = errorCode;
            return result;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    }
    
    /**
     * 批量操作结果
     */
    class BatchOperationResult {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<FailureDetail> failures;
        private long elapsedTime;
        private boolean completed;
        
        public BatchOperationResult() {
            this.failures = new java.util.ArrayList<>();
        }
        
        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public List<FailureDetail> getFailures() { return failures; }
        public void setFailures(List<FailureDetail> failures) { this.failures = failures; }
        
        public long getElapsedTime() { return elapsedTime; }
        public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        
        public void addFailure(Object id, String message, String errorCode) {
            FailureDetail detail = new FailureDetail();
            detail.setId(id);
            detail.setMessage(message);
            detail.setErrorCode(errorCode);
            failures.add(detail);
            failureCount++;
        }
    }
    
    /**
     * 失败详情
     */
    class FailureDetail {
        private Object id;
        private String message;
        private String errorCode;
        
        // Getters and Setters
        public Object getId() { return id; }
        public void setId(Object id) { this.id = id; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    }
    
    /**
     * 批量操作进度
     */
    class BatchProgress {
        private int totalCount;
        private int processedCount;
        private int successCount;
        private int failureCount;
        private int percentage;
        private long elapsedTime;
        private long estimatedTime;
        private String currentBatch;
        
        public BatchProgress(int totalCount, int processedCount, int successCount, int failureCount) {
            this.totalCount = totalCount;
            this.processedCount = processedCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.percentage = totalCount > 0 ? (processedCount * 100 / totalCount) : 0;
        }
        
        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public int getPercentage() { return percentage; }
        public void setPercentage(int percentage) { this.percentage = percentage; }
        
        public long getElapsedTime() { return elapsedTime; }
        public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }
        
        public long getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(long estimatedTime) { this.estimatedTime = estimatedTime; }
        
        public String getCurrentBatch() { return currentBatch; }
        public void setCurrentBatch(String currentBatch) { this.currentBatch = currentBatch; }
    }
}
