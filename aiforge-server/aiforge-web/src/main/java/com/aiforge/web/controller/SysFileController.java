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

    /**
     * 删除本地文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除本地文件")
    @DeleteMapping("/delete/{fileId}")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return Result.success();
    }

    /**
     * 上传文件到阿里云 OSS
     *
     * @param file      上传的文件
     * @param directory 存储目录
     * @return 文件信息
     */
    @Operation(summary = "上传文件到OSS")
    @PostMapping("/upload/oss")
    public Result<SysFile> uploadToOss(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "directory", required = false) String directory) {
        SysFile sysFile = fileService.uploadToOss(file, directory);
        return Result.success(sysFile);
    }

    /**
     * 从阿里云 OSS 下载文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     */
    @Operation(summary = "从OSS下载文件")
    @GetMapping("/download/oss/{fileId}")
    public void downloadFromOss(@PathVariable Long fileId, HttpServletResponse response) {
        fileService.downloadFromOss(fileId, response);
    }

    /**
     * 删除阿里云 OSS 中的文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除OSS文件")
    @DeleteMapping("/delete/oss/{fileId}")
    public Result<Void> deleteFromOss(@PathVariable Long fileId) {
        fileService.deleteFromOss(fileId);
        return Result.success();
    }
}
