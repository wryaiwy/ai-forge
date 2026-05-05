package com.aiforge.system.service.impl;

import com.aiforge.common.exception.AiForgeException;
import com.aiforge.system.entity.SysFile;
import com.aiforge.system.mapper.SysFileMapper;
import com.aiforge.system.service.SysFileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @Author: wengrunyang
 * @Description: 文件服务实现类
 * @DateTime: 2026/5/2 16:00
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class SysFileServiceImpl implements SysFileService {

    private final SysFileMapper fileMapper;

    /** 文件存储根路径 */
    @Value("${aiforge.file.storage-path:./uploads}")
    private String storagePath;

    /**
     * 文件上传
     */
    @Override
    public SysFile upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AiForgeException(400, "上传文件不能为空");
        }

        // 1. 获取文件信息
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String suffix = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex > 0) {
            suffix = originalName.substring(dotIndex + 1).toLowerCase();
        }

        // 2. 生成存储文件名（UUID + 后缀）
        String storageName = UUID.randomUUID().toString().replaceAll("-", "")
                + (suffix.isEmpty() ? "" : "." + suffix);

        // 3. 按日期分目录：uploads/2026/05/02/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + storageName;

        // 4. 创建目标目录
        File destDir = new File(storagePath, datePath);
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new AiForgeException(500, "创建文件存储目录失败");
        }

        // 5. 写入磁盘
        File destFile = new File(destDir, storageName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalName, e);
            throw new AiForgeException(500, "文件上传失败: " + e.getMessage());
        }

        // 6. 保存文件元信息到数据库
        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setStorageName(storageName);
        sysFile.setStoragePath(relativePath);
        sysFile.setUrl("/system/file/download/" + null); // 先插入拿ID后再更新
        sysFile.setFileSize(file.getSize());
        sysFile.setMimeType(file.getContentType());
        sysFile.setSuffix(suffix);

        fileMapper.insert(sysFile);

        // 7. 回填下载URL
        sysFile.setUrl("/system/file/download/" + sysFile.getFileId());
        fileMapper.updateById(sysFile);

        log.info("文件上传成功: {} -> {}", originalName, relativePath);
        return sysFile;
    }

    /**
     * 文件下载
     */
    @Override
    public void download(Long fileId, HttpServletResponse response) {
        // 1. 查询文件信息
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new AiForgeException(404, "文件不存在");
        }

        // 2. 定位文件
        File file = new File(storagePath, sysFile.getStoragePath());
        if (!file.exists()) {
            log.error("文件不存在于磁盘: {}", file.getAbsolutePath());
            throw new AiForgeException(404, "文件已被删除");
        }

        // 3. 设置响应头
        response.setContentType(sysFile.getMimeType() != null
                ? sysFile.getMimeType() : "application/octet-stream");
        response.setContentLengthLong(file.length());

        // Content-Disposition: 中文文件名需要URL编码
        String encodedName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

        // 4. 流式输出
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            log.error("文件下载失败: fileId={}", fileId, e);
            throw new AiForgeException(500, "文件下载失败");
        }
    }
}
