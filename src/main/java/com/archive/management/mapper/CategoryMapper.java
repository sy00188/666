package com.archive.management.mapper;

import com.archive.management.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分类Mapper接口
 * 基于MyBatis-Plus的分类数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据分类编码查找分类
     * 
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    @Select("SELECT * FROM category WHERE category_code = #{categoryCode} AND deleted = 0")
    Category findByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 根据分类名称查找分类
     * 
     * @param name 分类名称
     * @return 分类信息
     */
    @Select("SELECT * FROM category WHERE name = #{name} AND deleted = 0")
    Category findByName(@Param("name") String name);

    /**
     * 检查分类编码是否存在
     * 
     * @param categoryCode 分类编码
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE category_code = #{categoryCode} AND deleted = 0")
    int countByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 检查分类名称是否存在
     * 
     * @param name 分类名称
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE name = #{name} AND deleted = 0")
    int countByName(@Param("name") String name);

    /**
     * 根据状态查找分类列表
     * 
     * @param status 分类状态
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE status = #{status} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findByStatus(@Param("status") Integer status);

    /**
     * 根据分类类型查找分类列表
     * 
     * @param categoryType 分类类型
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE category_type = #{categoryType} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findByCategoryType(@Param("categoryType") Integer categoryType);

    /**
     * 根据父分类ID查找子分类列表
     * 
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查找根分类列表（顶级分类）
     * 
     * @return 根分类列表
     */
    @Select("SELECT * FROM category WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findRootCategories();

    /**
     * 查找启用的分类列表
     * 
     * @return 启用的分类列表
     */
    @Select("SELECT * FROM category WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findEnabledCategories();

    /**
     * 查找禁用的分类列表
     * 
     * @return 禁用的分类列表
     */
    @Select("SELECT * FROM category WHERE status = 0 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findDisabledCategories();

    /**
     * 根据分类级别查找分类列表
     * 
     * @param level 分类级别
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE level = #{level} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> findByLevel(@Param("level") Integer level);

    /**
     * 根据分类级别范围查找分类列表
     * 
     * @param minLevel 最小级别
     * @param maxLevel 最大级别
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE level BETWEEN #{minLevel} AND #{maxLevel} " +
            "AND deleted = 0 ORDER BY level ASC, sort_order ASC")
    List<Category> findByLevelBetween(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /**
     * 根据创建人查找分类列表
     * 
     * @param createdBy 创建人ID
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE created_by = #{createdBy} AND deleted = 0 ORDER BY create_time DESC")
    List<Category> findByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 根据创建时间范围查找分类列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Category> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 模糊搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    @Select("SELECT * FROM category WHERE (category_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Category> searchCategories(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索分类
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM category WHERE (category_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    IPage<Category> searchCategoriesWithPagination(Page<Category> page, @Param("keyword") String keyword);

    /**
     * 获取分类树结构
     * 
     * @return 分类树列表
     */
    @Select("SELECT * FROM category WHERE deleted = 0 ORDER BY parent_id ASC, sort_order ASC, create_time DESC")
    List<Category> getCategoryTree();

    /**
     * 获取启用的分类树结构
     * 
     * @return 启用的分类树列表
     */
    @Select("SELECT * FROM category WHERE status = 1 AND deleted = 0 ORDER BY parent_id ASC, sort_order ASC, create_time DESC")
    List<Category> getEnabledCategoryTree();

    /**
     * 获取指定分类的完整路径
     * 
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    @Select("WITH RECURSIVE category_path AS (" +
            "  SELECT id, name, parent_id, 0 as level FROM category WHERE id = #{categoryId}" +
            "  UNION ALL" +
            "  SELECT c.id, c.name, c.parent_id, cp.level + 1 FROM category c " +
            "  INNER JOIN category_path cp ON c.id = cp.parent_id" +
            ") SELECT id, name FROM category_path ORDER BY level DESC")
    List<Map<String, Object>> getCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    @Select("SELECT COUNT(*) FROM category WHERE deleted = 0")
    long countCategories();

    /**
     * 根据状态统计分类数量
     * 
     * @param status 状态
     * @return 分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据分类类型统计分类数量
     * 
     * @param categoryType 分类类型
     * @return 分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE category_type = #{categoryType} AND deleted = 0")
    long countByCategoryType(@Param("categoryType") Integer categoryType);

    /**
     * 根据分类级别统计分类数量
     * 
     * @param level 分类级别
     * @return 分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE level = #{level} AND deleted = 0")
    long countByLevel(@Param("level") Integer level);

    /**
     * 统计今日新增分类数量
     * 
     * @return 今日新增分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayNewCategories();

    /**
     * 统计本周新增分类数量
     * 
     * @return 本周新增分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekNewCategories();

    /**
     * 统计本月新增分类数量
     * 
     * @return 本月新增分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthNewCategories();

    /**
     * 批量更新分类状态
     * 
     * @param categoryIds 分类ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE category SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='categoryIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("categoryIds") List<Long> categoryIds, @Param("status") Integer status);

    /**
     * 批量删除分类（软删除）
     * 
     * @param categoryIds 分类ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE category SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='categoryIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteCategories(@Param("categoryIds") List<Long> categoryIds, @Param("deletedBy") Long deletedBy);

    /**
     * 更新分类排序
     * 
     * @param categoryId 分类ID
     * @param sortOrder 排序号
     * @return 更新数量
     */
    @Update("UPDATE category SET sort_order = #{sortOrder}, update_time = NOW() WHERE id = #{categoryId}")
    int updateSortOrder(@Param("categoryId") Long categoryId, @Param("sortOrder") Integer sortOrder);

    /**
     * 更新分类档案数量
     * 
     * @param categoryId 分类ID
     * @param archiveCount 档案数量
     * @return 更新数量
     */
    @Update("UPDATE category SET archive_count = #{archiveCount}, update_time = NOW() WHERE id = #{categoryId}")
    int updateArchiveCount(@Param("categoryId") Long categoryId, @Param("archiveCount") Integer archiveCount);

    /**
     * 增加分类档案数量
     * 
     * @param categoryId 分类ID
     * @param increment 增量
     * @return 更新数量
     */
    @Update("UPDATE category SET archive_count = archive_count + #{increment}, update_time = NOW() WHERE id = #{categoryId}")
    int incrementArchiveCount(@Param("categoryId") Long categoryId, @Param("increment") Integer increment);

    /**
     * 减少分类档案数量
     * 
     * @param categoryId 分类ID
     * @param decrement 减量
     * @return 更新数量
     */
    @Update("UPDATE category SET archive_count = GREATEST(archive_count - #{decrement}, 0), update_time = NOW() WHERE id = #{categoryId}")
    int decrementArchiveCount(@Param("categoryId") Long categoryId, @Param("decrement") Integer decrement);

    /**
     * 更新分类存储空间
     * 
     * @param categoryId 分类ID
     * @param storageSize 存储空间
     * @return 更新数量
     */
    @Update("UPDATE category SET storage_size = #{storageSize}, update_time = NOW() WHERE id = #{categoryId}")
    int updateStorageSize(@Param("categoryId") Long categoryId, @Param("storageSize") Long storageSize);

    /**
     * 增加分类存储空间
     * 
     * @param categoryId 分类ID
     * @param increment 增量
     * @return 更新数量
     */
    @Update("UPDATE category SET storage_size = storage_size + #{increment}, update_time = NOW() WHERE id = #{categoryId}")
    int incrementStorageSize(@Param("categoryId") Long categoryId, @Param("increment") Long increment);

    /**
     * 减少分类存储空间
     * 
     * @param categoryId 分类ID
     * @param decrement 减量
     * @return 更新数量
     */
    @Update("UPDATE category SET storage_size = GREATEST(storage_size - #{decrement}, 0), update_time = NOW() WHERE id = #{categoryId}")
    int decrementStorageSize(@Param("categoryId") Long categoryId, @Param("decrement") Long decrement);

    /**
     * 获取子分类数量
     * 
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    long getChildrenCount(@Param("parentId") Long parentId);

    /**
     * 获取分类的所有子分类ID
     * 
     * @param parentId 父分类ID
     * @return 子分类ID列表
     */
    @Select("SELECT id FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    List<Long> getChildrenIds(@Param("parentId") Long parentId);

    /**
     * 获取分类的所有祖先分类ID
     * 
     * @param categoryId 分类ID
     * @return 祖先分类ID列表
     */
    @Select("WITH RECURSIVE category_ancestors AS (" +
            "  SELECT parent_id FROM category WHERE id = #{categoryId} AND parent_id IS NOT NULL" +
            "  UNION ALL" +
            "  SELECT c.parent_id FROM category c " +
            "  INNER JOIN category_ancestors ca ON c.id = ca.parent_id " +
            "  WHERE c.parent_id IS NOT NULL" +
            ") SELECT parent_id FROM category_ancestors")
    List<Long> getAncestorIds(@Param("categoryId") Long categoryId);

    /**
     * 获取分类的所有后代分类ID
     * 
     * @param parentId 父分类ID
     * @return 后代分类ID列表
     */
    @Select("WITH RECURSIVE category_descendants AS (" +
            "  SELECT id FROM category WHERE parent_id = #{parentId} AND deleted = 0" +
            "  UNION ALL" +
            "  SELECT c.id FROM category c " +
            "  INNER JOIN category_descendants cd ON c.parent_id = cd.id " +
            "  WHERE c.deleted = 0" +
            ") SELECT id FROM category_descendants")
    List<Long> getDescendantIds(@Param("parentId") Long parentId);

    /**
     * 检查分类是否有子分类
     * 
     * @param categoryId 分类ID
     * @return 是否有子分类
     */
    @Select("SELECT COUNT(*) > 0 FROM category WHERE parent_id = #{categoryId} AND deleted = 0")
    boolean hasChildren(@Param("categoryId") Long categoryId);

    /**
     * 检查分类是否有档案
     * 
     * @param categoryId 分类ID
     * @return 是否有档案
     */
    @Select("SELECT COUNT(*) > 0 FROM archive WHERE category_id = #{categoryId} AND deleted = 0")
    boolean hasArchives(@Param("categoryId") Long categoryId);

    /**
     * 获取最大排序号
     * 
     * @param parentId 父分类ID
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 获取下一个可用的排序号
     * 
     * @param parentId 父分类ID
     * @return 下一个排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) + 1 FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getNextSortOrder(@Param("parentId") Long parentId);

    /**
     * 清理空分类
     * 
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE category SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE archive_count = 0 AND id NOT IN (" +
            "  SELECT DISTINCT parent_id FROM category WHERE parent_id IS NOT NULL AND deleted = 0" +
            ") AND status = 0 AND deleted = 0")
    int cleanEmptyCategories(@Param("deletedBy") Long deletedBy);

    /**
     * 重新计算分类档案数量
     * 
     * @param categoryId 分类ID
     * @return 更新数量
     */
    @Update("UPDATE category SET archive_count = (" +
            "  SELECT COUNT(*) FROM archive WHERE category_id = #{categoryId} AND deleted = 0" +
            "), update_time = NOW() WHERE id = #{categoryId}")
    int recalculateArchiveCount(@Param("categoryId") Long categoryId);

    /**
     * 重新计算分类存储空间
     * 
     * @param categoryId 分类ID
     * @return 更新数量
     */
    @Update("UPDATE category SET storage_size = (" +
            "  SELECT COALESCE(SUM(file_size), 0) FROM archive WHERE category_id = #{categoryId} AND deleted = 0" +
            "), update_time = NOW() WHERE id = #{categoryId}")
    int recalculateStorageSize(@Param("categoryId") Long categoryId);

    /**
     * 批量重新计算所有分类的档案数量和存储空间
     * 
     * @return 更新数量
     */
    @Update("UPDATE category c SET " +
            "archive_count = (SELECT COUNT(*) FROM archive a WHERE a.category_id = c.id AND a.deleted = 0), " +
            "storage_size = (SELECT COALESCE(SUM(a.file_size), 0) FROM archive a WHERE a.category_id = c.id AND a.deleted = 0), " +
            "update_time = NOW() " +
            "WHERE c.deleted = 0")
    int batchRecalculateStatistics();

    /**
     * 获取分类统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalCategories, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabledCategories, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabledCategories, " +
            "SUM(CASE WHEN category_type = 1 THEN 1 ELSE 0 END) as documentCategories, " +
            "SUM(CASE WHEN category_type = 2 THEN 1 ELSE 0 END) as imageCategories, " +
            "SUM(CASE WHEN category_type = 3 THEN 1 ELSE 0 END) as videoCategories, " +
            "SUM(CASE WHEN category_type = 4 THEN 1 ELSE 0 END) as audioCategories, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewCategories, " +
            "SUM(archive_count) as totalArchives, " +
            "SUM(storage_size) as totalStorageSize " +
            "FROM category WHERE deleted = 0")
    Map<String, Object> getCategoryStatistics();

    /**
     * 获取分类级别统计
     * 
     * @return 分类级别统计
     */
    @Select("SELECT level, COUNT(*) as categoryCount, SUM(archive_count) as totalArchives " +
            "FROM category WHERE deleted = 0 " +
            "GROUP BY level ORDER BY level ASC")
    List<Map<String, Object>> getCategoryLevelStatistics();

    /**
     * 获取分类类型统计
     * 
     * @return 分类类型统计
     */
    @Select("SELECT " +
            "CASE category_type " +
            "  WHEN 1 THEN '文档分类' " +
            "  WHEN 2 THEN '图片分类' " +
            "  WHEN 3 THEN '视频分类' " +
            "  WHEN 4 THEN '音频分类' " +
            "  ELSE '其他分类' " +
            "END as categoryTypeName, " +
            "COUNT(*) as categoryCount, " +
            "SUM(archive_count) as totalArchives, " +
            "SUM(storage_size) as totalStorageSize " +
            "FROM category WHERE deleted = 0 " +
            "GROUP BY category_type ORDER BY category_type ASC")
    List<Map<String, Object>> getCategoryTypeStatistics();

    /**
     * 获取分类使用统计
     * 
     * @return 分类使用统计
     */
    @Select("SELECT name as categoryName, archive_count, storage_size " +
            "FROM category WHERE deleted = 0 " +
            "ORDER BY archive_count DESC, storage_size DESC")
    List<Map<String, Object>> getCategoryUsageStatistics();

    /**
     * 获取分类创建趋势统计
     * 
     * @param days 天数
     * @return 创建趋势统计
     */
    @Select("SELECT DATE(create_time) as createDate, COUNT(*) as categoryCount " +
            "FROM category WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(create_time) ORDER BY createDate ASC")
    List<Map<String, Object>> getCategoryCreationTrendStatistics(@Param("days") int days);

    /**
     * 获取热门分类统计
     * 
     * @param limit 限制数量
     * @return 热门分类统计
     */
    @Select("SELECT name as categoryName, archive_count, storage_size " +
            "FROM category WHERE deleted = 0 AND archive_count > 0 " +
            "ORDER BY archive_count DESC, storage_size DESC LIMIT #{limit}")
    List<Map<String, Object>> getPopularCategoryStatistics(@Param("limit") int limit);

    /**
     * 获取存储空间最大的分类统计
     * 
     * @param limit 限制数量
     * @return 存储空间统计
     */
    @Select("SELECT name as categoryName, archive_count, storage_size " +
            "FROM category WHERE deleted = 0 AND storage_size > 0 " +
            "ORDER BY storage_size DESC, archive_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getLargestStorageCategoryStatistics(@Param("limit") int limit);
}