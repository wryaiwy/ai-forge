package com.aiforge.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @Author: wengrunyang
 * @Description: sys_file 文件信息实体类
 * @DateTime: 2026/5/2 16:00
 **/
@Data
@TableName("sys_file")
public class SysFile {

    /** 文件ID */
    @TableId(type = IdType.AUTO)
    private Long fileId;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名（UUID防重复） */
    private String storageName;

    /** 存储路径（相对于根目录） */
    private String storagePath;

    /** 文件访问URL */
    private String url;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件MIME类型 */
    private String mimeType;

    /** 文件后缀名 */
    private String suffix;

    /** 上传用户ID */
    private Long uploadUserId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
