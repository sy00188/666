package com.archive.management;

import com.archive.management.entity.Archive;
import com.archive.management.entity.Category;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试数据工厂类
 * 提供测试数据的创建和管理
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class TestDataFactory {

    private static final Random random = new Random();

    /**
     * 创建测试用户
     */
    public static User createTestUser() {
        return createTestUser(1L);
    }

    /**
     * 创建测试用户
     * @param id 用户ID
     */
    public static User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser" + id);
        user.setEmail("test" + id + "@example.com");
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // 123456
        user.setRealName("测试用户" + id);
        user.setPhone("1380013800" + String.format("%02d", id % 100));
        user.setStatus(1);
        user.setDeleted(false);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 创建测试用户列表
     * @param count 用户数量
     */
    public static List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createTestUser((long) i));
        }
        return users;
    }

    /**
     * 创建测试分类
     */
    public static Category createTestCategory() {
        return createTestCategory(1L);
    }

    /**
     * 创建测试分类
     * @param id 分类ID
     */
    public static Category createTestCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("测试分类" + id);
        category.setCode("TEST" + id);
        category.setDescription("这是一个测试分类");
        category.setParentId(0L);
        category.setSortOrder(id.intValue());
        category.setStatus(1);
        category.setDeleted(false);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }

    /**
     * 创建测试分类列表
     * @param count 分类数量
     */
    public static List<Category> createTestCategories(int count) {
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            categories.add(createTestCategory((long) i));
        }
        return categories;
    }

    /**
     * 创建测试档案
     */
    public static Archive createTestArchive() {
        return createTestArchive(1L);
    }

    /**
     * 创建测试档案
     * @param id 档案ID
     */
    public static Archive createTestArchive(Long id) {
        Archive archive = new Archive();
        archive.setId(id);
        archive.setTitle("测试档案" + id);
        archive.setDescription("这是一个测试档案的描述");
        archive.setArchiveNumber("ARC-" + String.format("%06d", id));
        archive.setType(ArchiveType.DOCUMENT);
        archive.setStatus(ArchiveStatus.ACTIVE.getValue());
        archive.setCategoryId(1L);
        archive.setCreatedBy(1L);
        archive.setViewCount(0L);
        archive.setDownloadCount(0L);
        archive.setVersionNumber(1);
        archive.setDeleted(false);
        archive.setCreateTime(LocalDateTime.now());
        archive.setUpdateTime(LocalDateTime.now());
        return archive;
    }

    /**
     * 创建测试档案列表
     * @param count 档案数量
     */
    public static List<Archive> createTestArchives(int count) {
        List<Archive> archives = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            archives.add(createTestArchive((long) i));
        }
        return archives;
    }

    /**
     * 创建随机测试档案
     */
    public static Archive createRandomTestArchive() {
        Archive archive = new Archive();
        archive.setTitle("随机档案" + random.nextInt(10000));
        archive.setDescription("这是一个随机生成的测试档案");
        archive.setArchiveNumber("ARC-" + System.currentTimeMillis() + "-" + random.nextInt(1000));
        archive.setType(ArchiveType.values()[random.nextInt(ArchiveType.values().length)]);
        archive.setStatus(ArchiveStatus.values()[random.nextInt(ArchiveStatus.values().length)].getValue());
        archive.setCategoryId((long) (random.nextInt(5) + 1));
        archive.setCreatedBy((long) (random.nextInt(10) + 1));
        archive.setViewCount((long) random.nextInt(1000));
        archive.setDownloadCount((long) random.nextInt(100));
        archive.setVersionNumber(random.nextInt(10) + 1);
        archive.setDeleted(false);
        archive.setCreateTime(LocalDateTime.now().minusDays(random.nextInt(365)));
        archive.setUpdateTime(LocalDateTime.now().minusDays(random.nextInt(30)));
        return archive;
    }

    /**
     * 创建随机测试档案列表
     * @param count 档案数量
     */
    public static List<Archive> createRandomTestArchives(int count) {
        List<Archive> archives = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            archives.add(createRandomTestArchive());
        }
        return archives;
    }

    /**
     * 生成随机字符串
     * @param length 字符串长度
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机邮箱
     */
    public static String generateRandomEmail() {
        return "test" + random.nextInt(10000) + "@example.com";
    }

    /**
     * 生成随机手机号
     */
    public static String generateRandomPhone() {
        return "138" + String.format("%08d", random.nextInt(100000000));
    }
}
