package com.archive.management.mapper;

import com.archive.management.entity.MetadataTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 元数据模板Mapper接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface MetadataTemplateMapper extends BaseMapper<MetadataTemplate> {

    /**
     * 根据分类ID查询模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE category_id = #{categoryId} AND status = 1 ORDER BY sort_order ASC")
    List<MetadataTemplate> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID和字段类型查询模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE category_id = #{categoryId} AND field_type = #{fieldType} AND status = 1 ORDER BY sort_order ASC")
    List<MetadataTemplate> selectByCategoryIdAndFieldType(@Param("categoryId") Long categoryId, @Param("fieldType") String fieldType);

    /**
     * 根据字段代码查询模板（用于唯一性检查）
     */
    @Select("SELECT * FROM arc_metadata_template WHERE field_code = #{fieldCode} AND category_id = #{categoryId} AND status = 1")
    MetadataTemplate selectByFieldCode(@Param("fieldCode") String fieldCode, @Param("categoryId") Long categoryId);

    /**
     * 查询必填字段模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE category_id = #{categoryId} AND is_required = 1 AND status = 1 ORDER BY sort_order ASC")
    List<MetadataTemplate> selectRequiredFields(@Param("categoryId") Long categoryId);

    /**
     * 查询可选字段模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE category_id = #{categoryId} AND is_required = 0 AND status = 1 ORDER BY sort_order ASC")
    List<MetadataTemplate> selectOptionalFields(@Param("categoryId") Long categoryId);

    /**
     * 根据字段类型统计数量
     */
    @Select("SELECT COUNT(*) FROM arc_metadata_template WHERE field_type = #{fieldType} AND status = 1")
    int countByFieldType(@Param("fieldType") String fieldType);

    /**
     * 根据分类ID统计模板数量
     */
    @Select("SELECT COUNT(*) FROM arc_metadata_template WHERE category_id = #{categoryId} AND status = 1")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 获取分类下的最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM arc_metadata_template WHERE category_id = #{categoryId}")
    Integer getMaxSortOrder(@Param("categoryId") Long categoryId);

    /**
     * 批量更新排序号
     */
    @Update("UPDATE arc_metadata_template SET sort_order = #{sortOrder} WHERE template_id = #{templateId}")
    int updateSortOrder(@Param("templateId") Long templateId, @Param("sortOrder") Integer sortOrder);

    /**
     * 根据分类ID软删除所有模板
     */
    @Update("UPDATE arc_metadata_template SET status = 0 WHERE category_id = #{categoryId}")
    int softDeleteByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 启用/禁用模板
     */
    @Update("UPDATE arc_metadata_template SET status = #{status} WHERE template_id = #{templateId}")
    int updateStatus(@Param("templateId") Long templateId, @Param("status") Integer status);

    /**
     * 分页查询模板列表（带搜索条件）
     */
    IPage<MetadataTemplate> selectPageWithConditions(Page<MetadataTemplate> page, 
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("fieldName") String fieldName,
                                                   @Param("fieldType") String fieldType,
                                                   @Param("isRequired") Integer isRequired,
                                                   @Param("status") Integer status);

    /**
     * 检查字段代码在分类下是否唯一
     */
    @Select("SELECT COUNT(*) FROM arc_metadata_template WHERE field_code = #{fieldCode} AND category_id = #{categoryId} AND template_id != #{excludeId} AND status = 1")
    int checkFieldCodeUnique(@Param("fieldCode") String fieldCode, 
                           @Param("categoryId") Long categoryId, 
                           @Param("excludeId") Long excludeId);

    /**
     * 根据创建用户ID查询模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE create_user_id = #{createUserId} AND status = 1 ORDER BY create_time DESC")
    List<MetadataTemplate> selectByCreateUserId(@Param("createUserId") Long createUserId);

    /**
     * 查询所有启用的模板（用于导出）
     */
    @Select("SELECT * FROM arc_metadata_template WHERE status = 1 ORDER BY category_id ASC, sort_order ASC")
    List<MetadataTemplate> selectAllEnabled();

    /**
     * 根据验证规则查询模板列表
     */
    @Select("SELECT * FROM arc_metadata_template WHERE validation_rule LIKE CONCAT('%', #{rule}, '%') AND status = 1")
    List<MetadataTemplate> selectByValidationRule(@Param("rule") String rule);

    /**
     * 统计各字段类型的使用情况
     */
    @Select("SELECT field_type, COUNT(*) as count FROM arc_metadata_template WHERE status = 1 GROUP BY field_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> getFieldTypeStatistics();

    /**
     * 查询最近创建的模板
     */
    @Select("SELECT * FROM arc_metadata_template WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<MetadataTemplate> selectRecentCreated(@Param("limit") int limit);

    /**
     * 复制模板到其他分类
     */
    @Select("SELECT * FROM arc_metadata_template WHERE template_id = #{templateId} AND status = 1")
    MetadataTemplate selectForCopy(@Param("templateId") Long templateId);
}