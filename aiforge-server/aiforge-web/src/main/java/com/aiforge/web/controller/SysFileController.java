package com.aiforge.web.controller;

import com.aiforge.common.result.Result;
import com.aiforge.system.entity.SysFile;
import com.aiforge.system.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: wengrunyang
 * @Description: 文件管理
 * @DateTime: 2026/5/2 16:00
 **/
@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService fileService;

    /**
     * 文件上传
     *
     * @param file 上传的文件
     * @return 文件信息
     */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result<SysFile> upload(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileService.upload(file);
        return Result.success(sysFile);
    }

    /**
     * 文件下载
     *
     * @param fileId 文件ID
     * @param response HTTP响应
     */
    @Operation(summary = "文件下载")
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable Long fileId, HttpServletResponse response) {
        fileService.download(fileId, response);
    }
}
