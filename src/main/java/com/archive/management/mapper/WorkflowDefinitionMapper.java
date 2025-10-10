package com.archive.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.archive.management.entity.WorkflowDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流定义Mapper接口
 */
@Mapper
public interface WorkflowDefinitionMapper extends BaseMapper<WorkflowDefinition> {
    
}

