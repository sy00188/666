package com.archive.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;

import com.archive.management.entity.User;
import com.archive.management.entity.Archive;
import com.archive.management.entity.Department;
import com.archive.management.entity.Role;
import com.archive.management.entity.Permission;
import com.archive.management.entity.Category;
import com.archive.management.repository.UserRepository;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.DepartmentRepository;
import com.archive.management.repository.RoleRepository;
import com.archive.management.repository.PermissionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基础测试类
 * 提供测试环境的基础配置和通用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ArchiveManagementApplication.class)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.yml")
public abstract class BaseTest {

    @Autowired
    protected TestEntityManager entityManager;

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
        // 测试前的通用设置
        System.setProperty("spring.application.name", "archive-management-system-test");
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
     * 清理测试数据
     * 子类可以重写此方法来实现特定的清理逻辑
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
     * 准备测试数据
     * 子类可以重写此方法来实现特定的数据准备逻辑
     */
    protected void prepareTestData() {
        // 创建基础测试数据
        createTestDepartments();
        createTestRoles();
        createTestPermissions();
        createTestUsers();
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
        entityManager.persistAndFlush(dept1);

        Department dept2 = new Department();
        dept2.setName("人事部");
        dept2.setCode("HR");
        dept2.setDescription("人力资源部门");
        dept2.setStatus(1);
        dept2.setCreateTime(LocalDateTime.now());
        dept2.setUpdateTime(LocalDateTime.now());
        entityManager.persistAndFlush(dept2);
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
        entityManager.persistAndFlush(adminRole);

        Role userRole = new Role();
        userRole.setName("普通用户");
        userRole.setCode("USER");
        userRole.setDescription("普通用户角色");
        userRole.setStatus(1);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        entityManager.persistAndFlush(userRole);
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
        entityManager.persistAndFlush(perm1);

        Permission perm2 = new Permission();
        perm2.setName("用户管理");
        perm2.setCode("USER_MANAGE");
        perm2.setDescription("用户管理权限");
        perm2.setType("MENU");
        perm2.setStatus(1);
        perm2.setCreateTime(LocalDateTime.now());
        perm2.setUpdateTime(LocalDateTime.now());
        entityManager.persistAndFlush(perm2);
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
        entityManager.persistAndFlush(adminUser);

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // 123456
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138001");
        testUser.setRealName("测试用户");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        entityManager.persistAndFlush(testUser);
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
        return entityManager.persistAndFlush(archive);
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
     * 创建测试数据列表
     */
    protected <T> List<T> createTestDataList(int count, TestDataFactory<T> factory) {
        List<T> dataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dataList.add(factory.create(i));
        }
        return dataList;
    }

    /**
     * 测试数据工厂接口
     */
    @FunctionalInterface
    public interface TestDataFactory<T> {
        T create(int index);
    }

    // ==================== Category测试支持方法 ====================

    /**
     * 创建测试分类
     */
    protected Category createTestCategory(String code, String name) {
        Category category = new Category();
        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setDescription("测试分类：" + name);
        category.setStatus(1);
        category.setLevel(1);
        category.setParentId(0L);
        category.setSortOrder(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }

    /**
     * 创建测试分类并持久化
     */
    protected Category createAndSaveTestCategory(String code, String name) {
        Category category = createTestCategory(code, name);
        return entityManager.persistAndFlush(category);
    }

    /**
     * 创建测试分类树结构
     * @param depth 深度
     * @param breadth 广度（每层节点数）
     * @return 根节点列表
     */
    protected List<Category> createTestCategoryTree(int depth, int breadth) {
        List<Category> roots = new ArrayList<>();
        for (int i = 0; i < breadth; i++) {
            Category root = createAndSaveTestCategory(
                "ROOT_" + i, 
                "根分类_" + i
            );
            roots.add(root);
            createChildCategories(root, depth - 1, breadth, "ROOT_" + i);
        }
        return roots;
    }

    /**
     * 递归创建子分类
     */
    private void createChildCategories(Category parent, int remainingDepth, 
                                       int breadth, String prefix) {
        if (remainingDepth <= 0) return;
        
        for (int i = 0; i < breadth; i++) {
            String code = prefix + "_" + i;
            Category child = createTestCategory(code, "分类_" + code);
            child.setParentId(parent.getId());
            child.setLevel(parent.getLevel() + 1);
            entityManager.persistAndFlush(child);
            
            createChildCategories(child, remainingDepth - 1, breadth, code);
        }
    }

    /**
     * 自定义断言：验证分类相等
     */
    protected void assertCategoryEquals(Category expected, Category actual) {
        assertThat(actual.getCategoryCode()).isEqualTo(expected.getCategoryCode());
        assertThat(actual.getCategoryName()).isEqualTo(expected.getCategoryName());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getLevel()).isEqualTo(expected.getLevel());
    }
}
