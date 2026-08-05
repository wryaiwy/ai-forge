package com.aiforge.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 *
 * @Author: wengrunyang
 * @DateTime: 2026/8/3
 */
@Data
@Component
@ConfigurationProperties(prefix = "aiforge.file.oss")
public class AliOssProperties {

    /** OSS 端点 */
    private String endpoint;

    /** Bucket 名称 */
    private String bucketName;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;
}
