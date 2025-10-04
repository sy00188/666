package com.archive.management.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import com.archive.management.service.MonitoringService;
import com.archive.management.service.CacheService;
import com.archive.management.service.ObservationService;
import com.archive.management.service.BatchOperationService;
import com.archive.management.service.AdvancedSearchService;

import static org.mockito.Mockito.*;

/**
 * 测试配置类
 * 提供测试环境的基础配置和Mock对象
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@TestConfiguration
@Profile("test")
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class TestConfiguration {

    /**
     * Mock监控服务
     */
    @Bean
    @Primary
    public MonitoringService mockMonitoringService() {
        MonitoringService mockService = mock(MonitoringService.class);
        
        // 配置Mock行为
        doNothing().when(mockService).recordUserRegistration();
        doNothing().when(mockService).recordDocumentCreated();
        doNothing().when(mockService).recordDocumentDownloaded();
        doNothing().when(mockService).recordUserLogin();
        doNothing().when(mockService).recordUserLogout();
        doNothing().when(mockService).recordDocumentSearched();
        doNothing().when(mockService).recordDocumentUpdated();
        doNothing().when(mockService).recordDocumentDeleted();
        doNothing().when(mockService).recordBatchOperation();
        doNothing().when(mockService).recordError();
        doNothing().when(mockService).recordError(anyString(), anyString());
        doNothing().when(mockService).incrementConcurrentOperations();
        doNothing().when(mockService).decrementConcurrentOperations();
        
        return mockService;
    }

    /**
     * Mock缓存服务
     */
    @Bean
    @Primary
    public CacheService mockCacheService() {
        CacheService mockService = mock(CacheService.class);
        
        // 配置Mock行为
        when(mockService.get(anyString(), anyString(), any(Class.class))).thenReturn(null);
        doNothing().when(mockService).put(anyString(), anyString(), any());
        doNothing().when(mockService).put(anyString(), anyString(), any(), anyLong(), any());
        doNothing().when(mockService).evict(anyString(), anyString());
        doNothing().when(mockService).clear(anyString());
        when(mockService.exists(anyString(), anyString())).thenReturn(false);
        when(mockService.getCacheSize(anyString())).thenReturn(0L);
        doNothing().when(mockService).clearAllCaches();
        
        return mockService;
    }

    /**
     * Mock观察服务
     */
    @Bean
    @Primary
    public ObservationService mockObservationService() {
        ObservationService mockService = mock(ObservationService.class);
        
        // 配置Mock行为
        when(mockService.observe(anyString(), any())).thenAnswer(invocation -> {
            Object operation = invocation.getArgument(1);
            if (operation instanceof java.util.function.Supplier) {
                return ((java.util.function.Supplier<?>) operation).get();
            }
            return null;
        });
        
        when(mockService.observe(anyString(), anyString(), anyString(), any())).thenAnswer(invocation -> {
            Object operation = invocation.getArgument(3);
            if (operation instanceof java.util.function.Supplier) {
                return ((java.util.function.Supplier<?>) operation).get();
            }
            return null;
        });
        
        doNothing().when(mockService).observe(anyString(), any(Runnable.class));
        doNothing().when(mockService).observe(anyString(), anyString(), anyString(), any(Runnable.class));
        
        return mockService;
    }

    /**
     * Mock批量操作服务
     */
    @Bean
    @Primary
    public BatchOperationService mockBatchOperationService() {
        BatchOperationService mockService = mock(BatchOperationService.class);
        
        // 配置Mock行为
        when(mockService.batchUpdateArchiveStatus(anyList(), any())).thenReturn(createMockResult());
        when(mockService.batchDeleteArchives(anyList())).thenReturn(createMockResult());
        when(mockService.batchUpdateUserStatus(anyList(), anyInt())).thenReturn(createMockResult());
        when(mockService.batchAssignRoles(anyList(), anyList())).thenReturn(createMockResult());
        when(mockService.batchAssignPermissions(anyList(), anyList())).thenReturn(createMockResult());
        
        return mockService;
    }

    /**
     * Mock高级搜索服务
     */
    @Bean
    @Primary
    public AdvancedSearchService mockAdvancedSearchService() {
        AdvancedSearchService mockService = mock(AdvancedSearchService.class);
        
        // 配置Mock行为
        when(mockService.advancedSearchArchives(any(), any())).thenReturn(org.springframework.data.domain.Page.empty());
        when(mockService.advancedSearchUsers(any(), any())).thenReturn(org.springframework.data.domain.Page.empty());
        when(mockService.fullTextSearchArchives(anyString(), anyInt())).thenReturn(java.util.Collections.emptyList());
        when(mockService.fullTextSearchUsers(anyString(), anyInt())).thenReturn(java.util.Collections.emptyList());
        when(mockService.getSearchSuggestions(anyString(), anyString())).thenReturn(java.util.Collections.emptyList());
        when(mockService.getSearchStatistics()).thenReturn(java.util.Collections.emptyMap());
        doNothing().when(mockService).recordSearchHistory(anyString(), anyString(), anyLong());
        when(mockService.getSearchHistory(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());
        doNothing().when(mockService).clearSearchHistory(anyLong(), anyString());
        
        return mockService;
    }

    /**
     * 创建Mock结果
     */
    private java.util.Map<String, Object> createMockResult() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("total", 0);
        result.put("successCount", 0);
        result.put("failedCount", 0);
        result.put("successIds", java.util.Collections.emptyList());
        result.put("failedIds", java.util.Collections.emptyList());
        return result;
    }
}
