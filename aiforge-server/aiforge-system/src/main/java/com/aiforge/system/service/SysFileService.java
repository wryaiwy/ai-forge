package com.aiforge.system.service;

import com.aiforge.system.entity.SysFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: wengrunyang
 * @Description: 文件服务接口
 * @DateTime: 2026/5/2 16:00
 **/
public interface SysFileService {

    /**
     * 文件上传
     *
     * @param file 上传的文件
     * @return 文件信息
     */
    SysFile upload(MultipartFile file);

    /**
     * 文件下载
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     */
    void download(Long fileId, HttpServletResponse response);

    /**
     * 删除本地文件
     *
     * @param fileId 文件ID
     */
    void delete(Long fileId);

    /**
     * 上传文件到阿里云 OSS
     *
     * @param file      上传的文件
     * @param directory 存储目录，如 "images/avatars"
     * @return 文件信息
     */
    SysFile uploadToOss(MultipartFile file, String directory);

    /**
     * 从阿里云 OSS 下载文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     */
    void downloadFromOss(Long fileId, HttpServletResponse response);

    /**
     * 删除阿里云 OSS 中的文件
     *
     * @param fileId 文件ID
     */
    void deleteFromOss(Long fileId);
}
