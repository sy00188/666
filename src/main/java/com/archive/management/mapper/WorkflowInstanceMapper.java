package com.archive.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.archive.management.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流实例Mapper接口
 */
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {
    
}

