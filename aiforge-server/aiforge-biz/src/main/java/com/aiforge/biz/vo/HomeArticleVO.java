package com.aiforge.biz.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 首页文章展示VO（轻量级，不含正文）
 **/
@Data
public class HomeArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long articleId;

    private String articleTitle;

    private String articleTags;

    private String authorName;

    private LocalDateTime publishTime;
}
