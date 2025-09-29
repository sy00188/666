package com.archive.management.repository;

import com.archive.management.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层接口
 * 提供分类实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    /**
     * 根据分类编码查找分类
     * @param code 分类编码
     * @return 分类信息
     */
    Optional<Category> findByCode(String code);

    /**
     * 根据分类名称查找分类
     * @param name 分类名称
     * @return 分类信息
     */
    Optional<Category> findByName(String name);

    /**
     * 检查分类编码是否存在
     * @param code 分类编码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查分类名称是否存在
     * @param name 分类名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据父分类ID查找子分类列表
     * @param parentId 父分类ID
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByParentId(Long parentId, Pageable pageable);

    /**
     * 查找所有根分类（父分类ID为空）
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByParentIdIsNull(Pageable pageable);

    /**
     * 根据分类状态查找分类列表
     * @param status 分类状态
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据分类级别查找分类列表
     * @param level 分类级别
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByLevel(Integer level, Pageable pageable);

    /**
     * 模糊查询分类（根据名称、描述）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Category> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据分类名称模糊查询
     * @param name 分类名称
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByNameContaining(String name, Pageable pageable);

    /**
     * 查找指定时间范围内创建的分类
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据排序号排序查找分类
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 查找指定父分类下的所有子分类（按排序号排序）
     * @param parentId 父分类ID
     * @return 分类列表
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId ORDER BY c.sortOrder ASC")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找所有根分类（按排序号排序）
     * @return 分类列表
     */
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL ORDER BY c.sortOrder ASC")
    List<Category> findAllRootCategories();

    /**
     * 查找指定分类的所有祖先分类
     * @param categoryId 分类ID
     * @return 祖先分类列表
     */
    @Query(value = "WITH RECURSIVE category_ancestors AS (" +
           "  SELECT id, parent_id, name, code, level FROM category WHERE id = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.id, c.parent_id, c.name, c.code, c.level " +
           "  FROM category c " +
           "  INNER JOIN category_ancestors ca ON c.id = ca.parent_id " +
           ") " +
           "SELECT * FROM category_ancestors WHERE id != :categoryId ORDER BY level", 
           nativeQuery = true)
    List<Category> findAncestors(@Param("categoryId") Long categoryId);

    /**
     * 查找指定分类的所有后代分类
     * @param categoryId 分类ID
     * @return 后代分类列表
     */
    @Query(value = "WITH RECURSIVE category_descendants AS (" +
           "  SELECT id, parent_id, name, code, level FROM category WHERE parent_id = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.id, c.parent_id, c.name, c.code, c.level " +
           "  FROM category c " +
           "  INNER JOIN category_descendants cd ON c.parent_id = cd.id " +
           ") " +
           "SELECT * FROM category_descendants ORDER BY level, sort_order", 
           nativeQuery = true)
    List<Category> findDescendants(@Param("categoryId") Long categoryId);

    /**
     * 统计分类总数
     * @return 分类总数
     */
    @Query("SELECT COUNT(c) FROM Category c")
    long countAllCategories();

    /**
     * 统计指定状态的分类数量
     * @param status 分类状态
     * @return 分类数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定级别的分类数量
     * @param level 分类级别
     * @return 分类数量
     */
    long countByLevel(Integer level);

    /**
     * 统计指定父分类下的子分类数量
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    long countByParentId(Long parentId);

    /**
     * 统计根分类数量
     * @return 根分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId IS NULL")
    long countRootCategories();

    /**
     * 统计指定时间范围内创建的分类数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查分类是否有子分类
     * @param categoryId 分类ID
     * @return 是否有子分类
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :categoryId")
    boolean hasChildren(@Param("categoryId") Long categoryId);

    /**
     * 检查分类是否有档案
     * @param categoryId 分类ID
     * @return 是否有档案
     */
    @Query("SELECT COUNT(a) > 0 FROM Archive a WHERE a.categoryId = :categoryId")
    boolean hasArchives(@Param("categoryId") Long categoryId);

    /**
     * 获取指定分类下的档案数量
     * @param categoryId 分类ID
     * @return 档案数量
     */
    @Query("SELECT COUNT(a) FROM Archive a WHERE a.categoryId = :categoryId")
    long getArchiveCount(@Param("categoryId") Long categoryId);

    /**
     * 获取指定分类及其所有子分类下的档案数量
     * @param categoryId 分类ID
     * @return 档案数量
     */
    @Query(value = "WITH RECURSIVE category_tree AS (" +
           "  SELECT id FROM category WHERE id = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.id FROM category c " +
           "  INNER JOIN category_tree ct ON c.parent_id = ct.id " +
           ") " +
           "SELECT COUNT(a.id) FROM archive a " +
           "WHERE a.category_id IN (SELECT id FROM category_tree)", 
           nativeQuery = true)
    long getTotalArchiveCount(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID列表批量查询分类
     * @param categoryIds 分类ID列表
     * @return 分类列表
     */
    List<Category> findByIdIn(List<Long> categoryIds);

    /**
     * 批量更新分类状态
     * @param categoryIds 分类ID列表
     * @param status 新状态
     */
    @Query("UPDATE Category c SET c.status = :status WHERE c.id IN :categoryIds")
    void batchUpdateStatus(@Param("categoryIds") List<Long> categoryIds, @Param("status") Integer status);

    /**
     * 更新分类的排序号
     * @param categoryId 分类ID
     * @param sortOrder 新排序号
     */
    @Query("UPDATE Category c SET c.sortOrder = :sortOrder WHERE c.id = :categoryId")
    void updateSortOrder(@Param("categoryId") Long categoryId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取指定父分类下的最大排序号
     * @param parentId 父分类ID
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("parentId") Long parentId);

    /**
     * 获取根分类的最大排序号
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.parentId IS NULL")
    Integer getMaxSortOrderForRoot();

    /**
     * 软删除分类（更新删除标记）
     * @param categoryId 分类ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Category c SET c.deleted = true, c.deletedBy = :deletedBy, c.deleteTime = :deleteTime WHERE c.id = :categoryId")
    void softDeleteCategory(@Param("categoryId") Long categoryId, 
                           @Param("deletedBy") Long deletedBy, 
                           @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的分类
     * @param pageable 分页参数
     * @return 分类分页列表
     */
    Page<Category> findByDeletedFalse(Pageable pageable);

    /**
     * 根据分类编码查找未删除的分类
     * @param code 分类编码
     * @return 分类信息
     */
    Optional<Category> findByCodeAndDeletedFalse(String code);

    /**
     * 根据分类名称查找未删除的分类
     * @param name 分类名称
     * @return 分类信息
     */
    Optional<Category> findByNameAndDeletedFalse(String name);

    /**
     * 查找未删除的根分类
     * @return 分类列表
     */
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL AND c.deleted = false ORDER BY c.sortOrder ASC")
    List<Category> findRootCategoriesNotDeleted();

    /**
     * 查找指定父分类下未删除的子分类
     * @param parentId 父分类ID
     * @return 分类列表
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.deleted = false ORDER BY c.sortOrder ASC")
    List<Category> findChildrenNotDeleted(@Param("parentId") Long parentId);

    /**
     * 按级别统计分类数量
     * @return 级别统计结果
     */
    @Query("SELECT c.level as level, COUNT(c) as count FROM Category c GROUP BY c.level ORDER BY level")
    List<Object[]> countCategoriesByLevel();

    /**
     * 按状态统计分类数量
     * @return 状态统计结果
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM Category c GROUP BY c.status ORDER BY status")
    List<Object[]> countCategoriesByStatus();

    /**
     * 获取分类树结构（包含档案数量）
     * @return 分类树统计结果
     */
    @Query("SELECT c.id, c.name, c.parentId, c.level, COUNT(a.id) as archiveCount " +
           "FROM Category c LEFT JOIN Archive a ON c.id = a.categoryId " +
           "WHERE c.deleted = false " +
           "GROUP BY c.id, c.name, c.parentId, c.level " +
           "ORDER BY c.level, c.sortOrder")
    List<Object[]> getCategoryTreeWithArchiveCount();
}