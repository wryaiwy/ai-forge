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
}
