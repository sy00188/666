package com.archive.management.sync;

import com.archive.management.BaseTest;
import com.archive.management.config.PermissionSyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 配置权限读取器测试
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@DisplayName("配置权限读取器测试")
class ConfigPermissionReaderTest extends BaseTest {

    @Mock
    private PermissionSyncConfig permissionSyncConfig;

    @InjectMocks
    private ConfigPermissionReader configPermissionReader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("读取配置文件权限")
    void shouldReadConfigPermissions() {
        // Given
        List<PermissionSyncConfig.PermissionConfig> configList = createTestPermissionConfigs();
        given(permissionSyncConfig.getPermissions()).willReturn(configList);

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).hasSize(4);
        
        // 验证系统管理权限
        PermissionDefinition systemManage = permissions.stream()
            .filter(p -> "SYSTEM_MANAGE".equals(p.getCode()))
            .findFirst()
            .orElse(null);
        
        assertThat(systemManage).isNotNull();
        assertThat(systemManage.getName()).isEqualTo("系统管理");
        assertThat(systemManage.getDescription()).isEqualTo("系统管理模块权限");
        assertThat(systemManage.getType()).isEqualTo("MENU");
        assertThat(systemManage.getPath()).isEqualTo("/system");
        assertThat(systemManage.getIcon()).isEqualTo("setting");
        assertThat(systemManage.getSort()).isEqualTo(1);
        assertThat(systemManage.getParentCode()).isNull();
        assertThat(systemManage.getSource()).isEqualTo("CONFIG");
    }

    @Test
    @DisplayName("读取空配置")
    void shouldHandleEmptyConfig() {
        // Given
        given(permissionSyncConfig.getPermissions()).willReturn(new ArrayList<>());

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("读取部分字段为空的配置")
    void shouldHandlePartialConfig() {
        // Given
        List<PermissionSyncConfig.PermissionConfig> configList = new ArrayList<>();
        
        PermissionSyncConfig.PermissionConfig config = new PermissionSyncConfig.PermissionConfig();
        config.setCode("PARTIAL_CONFIG");
        config.setName("部分配置");
        // 其他字段为null
        
        configList.add(config);
        given(permissionSyncConfig.getPermissions()).willReturn(configList);

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).hasSize(1);
        
        PermissionDefinition permission = permissions.get(0);
        assertThat(permission.getCode()).isEqualTo("PARTIAL_CONFIG");
        assertThat(permission.getName()).isEqualTo("部分配置");
        assertThat(permission.getDescription()).isNull();
        assertThat(permission.getType()).isNull();
        assertThat(permission.getPath()).isNull();
        assertThat(permission.getIcon()).isNull();
        assertThat(permission.getSort()).isNull();
        assertThat(permission.getParentCode()).isNull();
        assertThat(permission.getSource()).isEqualTo("CONFIG");
    }

    @Test
    @DisplayName("读取层级权限配置")
    void shouldReadHierarchicalPermissions() {
        // Given
        List<PermissionSyncConfig.PermissionConfig> configList = createHierarchicalPermissionConfigs();
        given(permissionSyncConfig.getPermissions()).willReturn(configList);

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).hasSize(3);
        
        // 验证父权限
        PermissionDefinition parentPermission = permissions.stream()
            .filter(p -> "USER_MANAGE".equals(p.getCode()))
            .findFirst()
            .orElse(null);
        
        assertThat(parentPermission).isNotNull();
        assertThat(parentPermission.getParentCode()).isEqualTo("SYSTEM_MANAGE");
        
        // 验证子权限
        PermissionDefinition childPermission = permissions.stream()
            .filter(p -> "USER_CREATE".equals(p.getCode()))
            .findFirst()
            .orElse(null);
        
        assertThat(childPermission).isNotNull();
        assertThat(childPermission.getParentCode()).isEqualTo("USER_MANAGE");
        assertThat(childPermission.getType()).isEqualTo("BUTTON");
    }

    @Test
    @DisplayName("读取不同类型的权限配置")
    void shouldReadDifferentPermissionTypes() {
        // Given
        List<PermissionSyncConfig.PermissionConfig> configList = createDifferentTypePermissionConfigs();
        given(permissionSyncConfig.getPermissions()).willReturn(configList);

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).hasSize(3);
        
        // 验证菜单类型
        boolean hasMenuType = permissions.stream()
            .anyMatch(p -> "MENU".equals(p.getType()));
        assertThat(hasMenuType).isTrue();
        
        // 验证按钮类型
        boolean hasButtonType = permissions.stream()
            .anyMatch(p -> "BUTTON".equals(p.getType()));
        assertThat(hasButtonType).isTrue();
        
        // 验证API类型
        boolean hasApiType = permissions.stream()
            .anyMatch(p -> "API".equals(p.getType()));
        assertThat(hasApiType).isTrue();
    }

    @Test
    @DisplayName("读取大量权限配置")
    void shouldReadLargePermissionConfig() {
        // Given
        List<PermissionSyncConfig.PermissionConfig> configList = createLargePermissionConfigs(100);
        given(permissionSyncConfig.getPermissions()).willReturn(configList);

        // When
        List<PermissionDefinition> permissions = configPermissionReader.readPermissions();

        // Then
        assertThat(permissions).hasSize(100);
        
        // 验证所有权限都有正确的源标识
        permissions.forEach(permission -> {
            assertThat(permission.getSource()).isEqualTo("CONFIG");
            assertThat(permission.getCode()).isNotBlank();
            assertThat(permission.getName()).isNotBlank();
        });
    }

    /**
     * 创建测试权限配置
     */
    private List<PermissionSyncConfig.PermissionConfig> createTestPermissionConfigs() {
        List<PermissionSyncConfig.PermissionConfig> configs = new ArrayList<>();
        
        // 系统管理
        PermissionSyncConfig.PermissionConfig systemManage = new PermissionSyncConfig.PermissionConfig();
        systemManage.setCode("SYSTEM_MANAGE");
        systemManage.setName("系统管理");
        systemManage.setDescription("系统管理模块权限");
        systemManage.setType("MENU");
        systemManage.setPath("/system");
        systemManage.setIcon("setting");
        systemManage.setSort(1);
        systemManage.setParentCode(null);
        configs.add(systemManage);
        
        // 用户管理
        PermissionSyncConfig.PermissionConfig userManage = new PermissionSyncConfig.PermissionConfig();
        userManage.setCode("USER_MANAGE");
        userManage.setName("用户管理");
        userManage.setDescription("用户管理权限");
        userManage.setType("MENU");
        userManage.setPath("/system/user");
        userManage.setIcon("user");
        userManage.setSort(2);
        userManage.setParentCode("SYSTEM_MANAGE");
        configs.add(userManage);
        
        // 创建用户
        PermissionSyncConfig.PermissionConfig userCreate = new PermissionSyncConfig.PermissionConfig();
        userCreate.setCode("USER_CREATE");
        userCreate.setName("创建用户");
        userCreate.setDescription("创建新用户权限");
        userCreate.setType("BUTTON");
        userCreate.setPath(null);
        userCreate.setIcon(null);
        userCreate.setSort(1);
        userCreate.setParentCode("USER_MANAGE");
        configs.add(userCreate);
        
        // 档案管理
        PermissionSyncConfig.PermissionConfig archiveManage = new PermissionSyncConfig.PermissionConfig();
        archiveManage.setCode("ARCHIVE_MANAGE");
        archiveManage.setName("档案管理");
        archiveManage.setDescription("档案管理模块权限");
        archiveManage.setType("MENU");
        archiveManage.setPath("/archive");
        archiveManage.setIcon("folder");
        archiveManage.setSort(3);
        archiveManage.setParentCode(null);
        configs.add(archiveManage);
        
        return configs;
    }

    /**
     * 创建层级权限配置
     */
    private List<PermissionSyncConfig.PermissionConfig> createHierarchicalPermissionConfigs() {
        List<PermissionSyncConfig.PermissionConfig> configs = new ArrayList<>();
        
        // 系统管理（顶级）
        PermissionSyncConfig.PermissionConfig systemManage = new PermissionSyncConfig.PermissionConfig();
        systemManage.setCode("SYSTEM_MANAGE");
        systemManage.setName("系统管理");
        systemManage.setType("MENU");
        systemManage.setParentCode(null);
        configs.add(systemManage);
        
        // 用户管理（二级）
        PermissionSyncConfig.PermissionConfig userManage = new PermissionSyncConfig.PermissionConfig();
        userManage.setCode("USER_MANAGE");
        userManage.setName("用户管理");
        userManage.setType("MENU");
        userManage.setParentCode("SYSTEM_MANAGE");
        configs.add(userManage);
        
        // 创建用户（三级）
        PermissionSyncConfig.PermissionConfig userCreate = new PermissionSyncConfig.PermissionConfig();
        userCreate.setCode("USER_CREATE");
        userCreate.setName("创建用户");
        userCreate.setType("BUTTON");
        userCreate.setParentCode("USER_MANAGE");
        configs.add(userCreate);
        
        return configs;
    }

    /**
     * 创建不同类型的权限配置
     */
    private List<PermissionSyncConfig.PermissionConfig> createDifferentTypePermissionConfigs() {
        List<PermissionSyncConfig.PermissionConfig> configs = new ArrayList<>();
        
        // 菜单类型
        PermissionSyncConfig.PermissionConfig menuConfig = new PermissionSyncConfig.PermissionConfig();
        menuConfig.setCode("MENU_PERMISSION");
        menuConfig.setName("菜单权限");
        menuConfig.setType("MENU");
        configs.add(menuConfig);
        
        // 按钮类型
        PermissionSyncConfig.PermissionConfig buttonConfig = new PermissionSyncConfig.PermissionConfig();
        buttonConfig.setCode("BUTTON_PERMISSION");
        buttonConfig.setName("按钮权限");
        buttonConfig.setType("BUTTON");
        configs.add(buttonConfig);
        
        // API类型
        PermissionSyncConfig.PermissionConfig apiConfig = new PermissionSyncConfig.PermissionConfig();
        apiConfig.setCode("API_PERMISSION");
        apiConfig.setName("API权限");
        apiConfig.setType("API");
        configs.add(apiConfig);
        
        return configs;
    }

    /**
     * 创建大量权限配置
     */
    private List<PermissionSyncConfig.PermissionConfig> createLargePermissionConfigs(int count) {
        List<PermissionSyncConfig.PermissionConfig> configs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            PermissionSyncConfig.PermissionConfig config = new PermissionSyncConfig.PermissionConfig();
            config.setCode("PERMISSION_" + String.format("%03d", i));
            config.setName("权限_" + i);
            config.setDescription("权限描述_" + i);
            config.setType(i % 3 == 0 ? "MENU" : (i % 3 == 1 ? "BUTTON" : "API"));
            config.setSort(i);
            configs.add(config);
        }
        
        return configs;
    }
}
