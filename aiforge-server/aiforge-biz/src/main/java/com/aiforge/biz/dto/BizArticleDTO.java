package com.aiforge.biz.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * @Author: wengrunyang
 * @Description: 文章发布DTO
 * @DateTime: 2026/5/5 10:25
 **/
@Data
public class BizArticleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文章标题 */
    private String articleTitle;

    /** 文章标签 */
    private String articleTags;

    /** 文章内容 */
    private String content;

    /** 发布状态 */
    private Integer publishStatus;
}
