package com.archive.management.sync;

import com.archive.management.BaseTest;
import com.archive.management.annotation.RequirePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 注解权限扫描器测试
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@DisplayName("注解权限扫描器测试")
class AnnotationPermissionScannerTest extends BaseTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private AnnotationPermissionScanner annotationPermissionScanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("扫描Controller中的权限注解")
    void shouldScanControllerPermissions() {
        // Given
        Map<String, Object> controllers = new HashMap<>();
        controllers.put("testController", new TestController());
        
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(controllers);
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(new HashMap<>());

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        assertThat(permissions).hasSize(3);
        
        // 验证扫描到的权限
        PermissionDefinition readPermission = permissions.stream()
            .filter(p -> "test:read".equals(p.getCode()))
            .findFirst()
            .orElse(null);
        
        assertThat(readPermission).isNotNull();
        assertThat(readPermission.getName()).isEqualTo("读取测试数据");
        assertThat(readPermission.getDescription()).isEqualTo("读取测试数据的权限");
        assertThat(readPermission.getType()).isEqualTo("API");
        assertThat(readPermission.getSource()).isEqualTo("ANNOTATION");
    }

    @Test
    @DisplayName("扫描空Controller")
    void shouldHandleEmptyControllers() {
        // Given
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(new HashMap<>());
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(new HashMap<>());

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("扫描没有权限注解的Controller")
    void shouldHandleControllerWithoutPermissions() {
        // Given
        Map<String, Object> controllers = new HashMap<>();
        controllers.put("emptyController", new EmptyController());
        
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(controllers);
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(new HashMap<>());

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("扫描代理类Controller")
    void shouldHandleProxyControllers() {
        // Given
        Map<String, Object> controllers = new HashMap<>();
        // 模拟Spring代理类（类名包含$$）
        TestController proxyController = new TestController() {};
        controllers.put("proxyController", proxyController);
        
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(controllers);
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(new HashMap<>());

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        // 应该能正确处理代理类并扫描到权限
        assertThat(permissions).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("扫描类级别权限注解")
    void shouldScanClassLevelPermissions() {
        // Given
        Map<String, Object> controllers = new HashMap<>();
        controllers.put("classLevelController", new ClassLevelPermissionController());
        
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(controllers);
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(new HashMap<>());

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        
        // 验证类级别权限被扫描到
        boolean hasClassPermission = permissions.stream()
            .anyMatch(p -> "admin:manage".equals(p.getCode()));
        assertThat(hasClassPermission).isTrue();
    }

    @Test
    @DisplayName("扫描RequestMapping注解的Controller")
    void shouldScanRequestMappingControllers() {
        // Given
        given(applicationContext.getBeansWithAnnotation(RestController.class))
            .willReturn(new HashMap<>());
        
        Map<String, Object> requestMappingControllers = new HashMap<>();
        requestMappingControllers.put("requestMappingController", new RequestMappingController());
        given(applicationContext.getBeansWithAnnotation(RequestMapping.class))
            .willReturn(requestMappingControllers);

        // When
        List<PermissionDefinition> permissions = annotationPermissionScanner.scanPermissions();

        // Then
        assertThat(permissions).isNotEmpty();
        
        // 验证RequestMapping Controller的权限被扫描到
        boolean hasRequestMappingPermission = permissions.stream()
            .anyMatch(p -> "legacy:access".equals(p.getCode()));
        assertThat(hasRequestMappingPermission).isTrue();
    }

    // ==================== 测试用的Controller类 ====================

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/read")
        @RequirePermission(value = "test:read", name = "读取测试数据", description = "读取测试数据的权限")
        public String read() {
            return "read";
        }

        @PostMapping("/write")
        @RequirePermission(value = "test:write", name = "写入测试数据", description = "写入测试数据的权限")
        public String write() {
            return "write";
        }

        @PostMapping("/delete")
        @RequirePermission("test:delete")
        public String delete() {
            return "delete";
        }

        // 没有权限注解的方法
        @GetMapping("/public")
        public String publicMethod() {
            return "public";
        }
    }

    @RestController
    static class EmptyController {
        
        @GetMapping("/empty")
        public String empty() {
            return "empty";
        }
    }

    @RestController
    @RequirePermission(value = "admin:manage", name = "管理员权限", description = "管理员管理权限")
    static class ClassLevelPermissionController {
        
        @GetMapping("/admin")
        public String admin() {
            return "admin";
        }
    }

    @RequestMapping("/legacy")
    static class RequestMappingController {
        
        @GetMapping("/access")
        @RequirePermission(value = "legacy:access", name = "遗留系统访问", description = "访问遗留系统的权限")
        public String access() {
            return "access";
        }
    }
}
