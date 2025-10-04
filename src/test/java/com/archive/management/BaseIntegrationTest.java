package com.archive.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.archive.management.entity.User;
import com.archive.management.entity.Archive;
import com.archive.management.entity.Department;
import com.archive.management.entity.Role;
import com.archive.management.entity.Permission;
import com.archive.management.repository.UserRepository;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.DepartmentRepository;
import com.archive.management.repository.RoleRepository;
import com.archive.management.repository.PermissionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/**
 * 集成测试基础类
 * 提供Web层集成测试的基础配置
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ArchiveManagementApplication.class)
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ArchiveRepository archiveRepository;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        // 集成测试前的通用设置
        System.setProperty("spring.application.name", "archive-management-system-integration-test");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.timezone", "Asia/Shanghai");
        
        // 准备测试数据
        prepareTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    /**
     * 准备测试数据
     */
    protected void prepareTestData() {
        // 创建基础测试数据
        createTestDepartments();
        createTestRoles();
        createTestPermissions();
        createTestUsers();
    }

    /**
     * 清理测试数据
     */
    protected void cleanupTestData() {
        // 清理所有测试数据
        archiveRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    /**
     * 创建测试部门
     */
    protected void createTestDepartments() {
        Department dept1 = new Department();
        dept1.setName("技术部");
        dept1.setCode("TECH");
        dept1.setDescription("技术开发部门");
        dept1.setStatus(1);
        dept1.setCreateTime(LocalDateTime.now());
        dept1.setUpdateTime(LocalDateTime.now());
        departmentRepository.save(dept1);

        Department dept2 = new Department();
        dept2.setName("人事部");
        dept2.setCode("HR");
        dept2.setDescription("人力资源部门");
        dept2.setStatus(1);
        dept2.setCreateTime(LocalDateTime.now());
        dept2.setUpdateTime(LocalDateTime.now());
        departmentRepository.save(dept2);
    }

    /**
     * 创建测试角色
     */
    protected void createTestRoles() {
        Role adminRole = new Role();
        adminRole.setName("系统管理员");
        adminRole.setCode("ADMIN");
        adminRole.setDescription("系统管理员角色");
        adminRole.setStatus(1);
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("普通用户");
        userRole.setCode("USER");
        userRole.setDescription("普通用户角色");
        userRole.setStatus(1);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        roleRepository.save(userRole);
    }

    /**
     * 创建测试权限
     */
    protected void createTestPermissions() {
        Permission perm1 = new Permission();
        perm1.setName("档案管理");
        perm1.setCode("ARCHIVE_MANAGE");
        perm1.setDescription("档案管理权限");
        perm1.setType("MENU");
        perm1.setStatus(1);
        perm1.setCreateTime(LocalDateTime.now());
        perm1.setUpdateTime(LocalDateTime.now());
        permissionRepository.save(perm1);

        Permission perm2 = new Permission();
        perm2.setName("用户管理");
        perm2.setCode("USER_MANAGE");
        perm2.setDescription("用户管理权限");
        perm2.setType("MENU");
        perm2.setStatus(1);
        perm2.setCreateTime(LocalDateTime.now());
        perm2.setUpdateTime(LocalDateTime.now());
        permissionRepository.save(perm2);
    }

    /**
     * 创建测试用户
     */
    protected void createTestUsers() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // 123456
        adminUser.setEmail("admin@example.com");
        adminUser.setPhone("13800138000");
        adminUser.setRealName("系统管理员");
        adminUser.setStatus(1);
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        userRepository.save(adminUser);

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // 123456
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138001");
        testUser.setRealName("测试用户");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        userRepository.save(testUser);
    }

    /**
     * 创建测试档案
     */
    protected Archive createTestArchive() {
        Archive archive = new Archive();
        archive.setTitle("测试档案");
        archive.setCode("TEST-001");
        archive.setDescription("这是一个测试档案");
        archive.setStatus(1);
        archive.setCreateTime(LocalDateTime.now());
        archive.setUpdateTime(LocalDateTime.now());
        return archiveRepository.save(archive);
    }

    /**
     * 获取测试用户
     */
    protected User getTestUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 获取测试部门
     */
    protected Department getTestDepartment(String code) {
        return departmentRepository.findByCode(code).orElse(null);
    }

    /**
     * 获取测试角色
     */
    protected Role getTestRole(String code) {
        return roleRepository.findByCode(code).orElse(null);
    }

    /**
     * 获取测试权限
     */
    protected Permission getTestPermission(String code) {
        return permissionRepository.findByCode(code).orElse(null);
    }

    /**
     * 创建JSON请求体
     */
    protected String createJsonRequest(Object request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 创建JSON请求体（使用JsonNode）
     */
    protected String createJsonRequest(JsonNode request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 创建空的JSON请求体
     */
    protected String createEmptyJsonRequest() throws Exception {
        return "{}";
    }

    /**
     * 创建分页请求参数
     */
    protected String createPageRequest(int page, int size) throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("page", page);
        request.put("size", size);
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 创建搜索请求参数
     */
    protected String createSearchRequest(String keyword, String type) throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("keyword", keyword);
        request.put("type", type);
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 执行GET请求
     */
    protected MockHttpServletRequestBuilder getRequest(String url) {
        return MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    /**
     * 执行POST请求
     */
    protected MockHttpServletRequestBuilder postRequest(String url, String content) {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content);
    }

    /**
     * 执行PUT请求
     */
    protected MockHttpServletRequestBuilder putRequest(String url, String content) {
        return MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content);
    }

    /**
     * 执行DELETE请求
     */
    protected MockHttpServletRequestBuilder deleteRequest(String url) {
        return MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    /**
     * 执行带认证的请求
     */
    protected MockHttpServletRequestBuilder authenticatedRequest(MockHttpServletRequestBuilder request) {
        return request.with(csrf());
    }

    /**
     * 执行带用户认证的请求
     */
    protected MockHttpServletRequestBuilder userAuthenticatedRequest(MockHttpServletRequestBuilder request) {
        return request.with(user("testuser").roles("USER"));
    }

    /**
     * 执行带管理员认证的请求
     */
    protected MockHttpServletRequestBuilder adminAuthenticatedRequest(MockHttpServletRequestBuilder request) {
        return request.with(user("admin").roles("ADMIN"));
    }

    /**
     * 验证成功响应
     */
    protected void expectSuccessResponse() throws Exception {
        // 子类可以重写此方法来实现特定的成功响应验证
    }

    /**
     * 验证错误响应
     */
    protected void expectErrorResponse(String errorCode) throws Exception {
        // 子类可以重写此方法来实现特定的错误响应验证
    }

    /**
     * 验证分页响应
     */
    protected void expectPageResponse(int expectedPage, int expectedSize) throws Exception {
        // 子类可以重写此方法来实现特定的分页响应验证
    }
}
