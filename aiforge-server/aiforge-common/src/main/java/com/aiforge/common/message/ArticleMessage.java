package com.aiforge.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMessage {

    /** 文章ID */
    private Long articleId;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 操作类型 */
    private String action;
}
