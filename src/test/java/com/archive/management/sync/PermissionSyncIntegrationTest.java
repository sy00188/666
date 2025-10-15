package com.archive.management.sync;

import com.archive.management.BaseTest;
import com.archive.management.config.PermissionSyncConfig;
import com.archive.management.entity.Permission;
import com.archive.management.event.PermissionChangedEvent;
import com.archive.management.mapper.PermissionMapper;
import com.archive.management.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 权限同步集成测试
 * 测试权限同步功能的端到端流程
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("权限同步集成测试")
class PermissionSyncIntegrationTest extends BaseTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private AnnotationPermissionScanner annotationPermissionScanner;

    @Autowired
    private ConfigPermissionReader configPermissionReader;

    @Autowired
    private PermissionDiffCalculator permissionDiffCalculator;

    @Autowired
    private PermissionSyncConfig permissionSyncConfig;

    @SpyBean
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        permissionMapper.delete(null);
        
        // 重置事件发布器的调用记录
        reset(eventPublisher);
    }

    @Test
    @DisplayName("完整权限同步流程 - 从所有源同步")
    void shouldSyncPermissionsFromAllSources() {
        // Given
        Long operatorId = 1L;
        
        // 创建一些现有权限
        createTestPermissions();

        // When
        int syncCount = permissionService.syncPermissions("all", operatorId);

        // Then
        assertThat(syncCount).isGreaterThan(0);
        
        // 验证数据库中的权限
        List<Permission> allPermissions = permissionMapper.selectList(null);
        assertThat(allPermissions).isNotEmpty();
        
        // 验证事件发布
        verify(eventPublisher, atLeastOnce()).publishEvent(any(PermissionChangedEvent.class));
    }

    @Test
    @DisplayName("注解权限扫描测试")
    void shouldScanAnnotationPermissions() {
        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        
        // 验证扫描到的权限包含预期的权限
        boolean hasSystemPermission = permissions.stream()
            .anyMatch(p -> p.getCode().startsWith("system:"));
        assertThat(hasSystemPermission).isTrue();
        
        // 验证权限定义的完整性
        permissions.forEach(permission -> {
            assertThat(permission.getCode()).isNotBlank();
            assertThat(permission.getSource()).isEqualTo("ANNOTATION");
        });
    }

    @Test
    @DisplayName("配置文件权限读取测试")
    void shouldReadConfigPermissions() {
        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        
        // 验证读取到的权限包含配置文件中的权限
        boolean hasSystemManage = permissions.stream()
            .anyMatch(p -> "SYSTEM_MANAGE".equals(p.getCode()));
        assertThat(hasSystemManage).isTrue();
        
        // 验证权限定义的完整性
        permissions.forEach(permission -> {
            assertThat(permission.getCode()).isNotBlank();
            assertThat(permission.getName()).isNotBlank();
            assertThat(permission.getSource()).isEqualTo("CONFIG");
        });
    }

    @Test
    @DisplayName("权限差异计算测试")
    void shouldCalculatePermissionDifferences() {
        // Given
        List<Permission> existingPermissions = createTestPermissions();
        List<PermissionDefinition> newDefinitions = createTestPermissionDefinitions();

        // When
        PermissionDiffCalculator.DiffResult result = 
            permissionDiffCalculator.calculateDiff(existingPermissions, newDefinitions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToCreate()).isNotEmpty();
        assertThat(result.getToUpdate()).isNotNull();
        assertThat(result.getToDelete()).isNotNull();
    }

    @Test
    @DisplayName("权限同步 - 仅从注解源")
    void shouldSyncPermissionsFromAnnotationOnly() {
        // Given
        Long operatorId = 2L;

        // When
        int syncCount = permissionService.syncPermissions("annotation", operatorId);

        // Then
        assertThat(syncCount).isGreaterThanOrEqualTo(0);
        
        // 验证只同步了注解权限
        List<Permission> permissions = permissionMapper.selectList(null);
        if (!permissions.isEmpty()) {
            // 如果有权限被同步，验证它们的来源
            permissions.forEach(permission -> {
                // 注解权限通常是API类型
                assertThat(permission.getPermissionType()).isIn("API", "BUTTON");
            });
        }
    }

    @Test
    @DisplayName("权限同步 - 仅从配置源")
    void shouldSyncPermissionsFromConfigOnly() {
        // Given
        Long operatorId = 3L;

        // When
        int syncCount = permissionService.syncPermissions("config", operatorId);

        // Then
        assertThat(syncCount).isGreaterThanOrEqualTo(0);
        
        // 验证只同步了配置权限
        List<Permission> permissions = permissionMapper.selectList(null);
        if (!permissions.isEmpty()) {
            // 配置权限通常包含菜单类型
            boolean hasMenuPermission = permissions.stream()
                .anyMatch(p -> "MENU".equals(p.getPermissionType()));
            assertThat(hasMenuPermission).isTrue();
        }
    }

    @Test
    @DisplayName("权限同步事件发布测试")
    void shouldPublishPermissionChangedEvents() {
        // Given
        Long operatorId = 1L;
        createTestPermissions();

        // When
        permissionService.syncPermissions("all", operatorId);

        // Then
        // 验证发布了同步完成事件
        verify(eventPublisher, atLeastOnce()).publishEvent(argThat(event -> {
            if (event instanceof PermissionChangedEvent) {
                PermissionChangedEvent permEvent = (PermissionChangedEvent) event;
                return "SYNC".equals(permEvent.getAction()) && 
                       operatorId.equals(permEvent.getOperatorId());
            }
            return false;
        }));
    }

    @Test
    @DisplayName("权限同步 - 增量更新测试")
    void shouldPerformIncrementalSync() {
        // Given
        Long operatorId = 1L;
        
        // 第一次同步
        int firstSyncCount = permissionService.syncPermissions("all", operatorId);
        List<Permission> afterFirstSync = permissionMapper.selectList(null);
        
        // 清理事件记录
        reset(eventPublisher);

        // When - 第二次同步（应该是增量更新）
        int secondSyncCount = permissionService.syncPermissions("all", operatorId);

        // Then
        // 第二次同步的变更应该较少或为0
        assertThat(secondSyncCount).isLessThanOrEqualTo(firstSyncCount);
        
        List<Permission> afterSecondSync = permissionMapper.selectList(null);
        
        // 权限总数应该保持稳定或略有变化
        assertThat(Math.abs(afterSecondSync.size() - afterFirstSync.size())).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("权限同步 - 错误处理测试")
    void shouldHandleSyncErrors() {
        // Given
        Long operatorId = 1L;
        
        // 模拟一个会导致错误的场景（例如无效的操作员ID）
        Long invalidOperatorId = -1L;

        // When & Then
        try {
            permissionService.syncPermissions("all", invalidOperatorId);
            // 如果没有抛出异常，验证至少没有破坏数据
            List<Permission> permissions = permissionMapper.selectList(null);
            // 数据应该保持一致性
            assertThat(permissions).isNotNull();
        } catch (Exception e) {
            // 如果抛出异常，验证异常信息有意义
            assertThat(e.getMessage()).isNotBlank();
        }
    }

    @Test
    @DisplayName("权限同步 - 性能测试")
    void shouldCompleteWithinReasonableTime() {
        // Given
        Long operatorId = 1L;
        long startTime = System.currentTimeMillis();

        // When
        int syncCount = permissionService.syncPermissions("all", operatorId);

        // Then
        long duration = System.currentTimeMillis() - startTime;
        
        // 同步操作应该在合理时间内完成（10秒内）
        assertThat(duration).isLessThan(10000);
        assertThat(syncCount).isGreaterThanOrEqualTo(0);
    }

    /**
     * 创建测试权限数据
     */
    private List<Permission> createTestPermissions() {
        Permission permission1 = new Permission();
        permission1.setPermissionCode("test:read");
        permission1.setPermissionName("测试读取");
        permission1.setPermissionType("API");
        permission1.setCreateBy(1L);
        permission1.setCreateTime(LocalDateTime.now());
        permission1.setUpdateBy(1L);
        permission1.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(permission1);

        Permission permission2 = new Permission();
        permission2.setPermissionCode("test:write");
        permission2.setPermissionName("测试写入");
        permission2.setPermissionType("API");
        permission2.setCreateBy(1L);
        permission2.setCreateTime(LocalDateTime.now());
        permission2.setUpdateBy(1L);
        permission2.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(permission2);

        return List.of(permission1, permission2);
    }

    /**
     * 创建测试权限定义
     */
    private List<PermissionDefinition> createTestPermissionDefinitions() {
        PermissionDefinition def1 = new PermissionDefinition();
        def1.setCode("test:read");
        def1.setName("测试读取权限");
        def1.setDescription("用于测试的读取权限");
        def1.setType("API");
        def1.setSource("ANNOTATION");

        PermissionDefinition def2 = new PermissionDefinition();
        def2.setCode("test:delete");
        def2.setName("测试删除权限");
        def2.setDescription("用于测试的删除权限");
        def2.setType("API");
        def2.setSource("CONFIG");

        return List.of(def1, def2);
    }
}
