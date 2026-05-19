package com.aiforge.system.service;

import com.aiforge.system.entity.SysOperLog;
import com.aiforge.system.vo.SysOperLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 操作日志服务
 */
public interface SysOperLogService {

    /**
     * 保存操作日志
     */
    void save(SysOperLog operLog);

    /**
     * 分页查询操作日志
     */
    IPage<SysOperLogVO> pageList(Page<SysOperLog> page, String module);
}
