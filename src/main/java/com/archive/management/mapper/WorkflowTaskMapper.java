package com.archive.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.archive.management.entity.WorkflowTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流任务Mapper接口
 */
@Mapper
public interface WorkflowTaskMapper extends BaseMapper<WorkflowTask> {
    
}

