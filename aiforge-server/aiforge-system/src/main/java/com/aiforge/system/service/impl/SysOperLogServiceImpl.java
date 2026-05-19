package com.aiforge.system.service.impl;

import com.aiforge.common.enums.OperBusinessTypeEnum;
import com.aiforge.common.enums.OperLogStatusEnum;
import com.aiforge.system.entity.SysOperLog;
import com.aiforge.system.mapper.SysOperLogMapper;
import com.aiforge.system.service.SysOperLogService;
import com.aiforge.system.vo.SysOperLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService {

    private final SysOperLogMapper operLogMapper;

    @Override
    public void save(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    @Override
    public IPage<SysOperLogVO> pageList(Page<SysOperLog> page, String module) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(module), SysOperLog::getModule, module)
                .orderByDesc(SysOperLog::getOperTime);

        return operLogMapper.selectPage(page, wrapper).convert(this::toVO);
    }

    private SysOperLogVO toVO(SysOperLog operLog) {
        SysOperLogVO vo = new SysOperLogVO();
        vo.setOperId(operLog.getOperId());
        vo.setUserId(operLog.getUserId());
        vo.setModule(operLog.getModule());
        vo.setBusinessType(operLog.getBusinessType());
        vo.setBusinessTypeDesc(OperBusinessTypeEnum.getDescByCode(operLog.getBusinessType()));
        vo.setMethod(operLog.getMethod());
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setRequestUrl(operLog.getRequestUrl());
        vo.setOperParam(operLog.getOperParam());
        vo.setStatus(operLog.getStatus());
        vo.setStatusDesc(OperLogStatusEnum.getDescByCode(operLog.getStatus()));
        vo.setErrorMsg(operLog.getErrorMsg());
        vo.setCostTime(operLog.getCostTime());
        vo.setOperTime(operLog.getOperTime());
        return vo;
    }
}
