package com.aiforge.web.controller;

import com.aiforge.common.result.Result;
import com.aiforge.system.entity.SysOperLog;
import com.aiforge.system.service.SysOperLogService;
import com.aiforge.system.vo.SysOperLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志管理
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    /**
     * 操作日志分页列表
     */
    @Operation(summary = "操作日志列表")
    @GetMapping("/list")
    public Result<IPage<SysOperLogVO>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String module) {
        return Result.success(operLogService.pageList(new Page<>(current, size), module));
    }
}
