package com.aiforge.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人中心文章列表 VO
 */
@Data
public class PersonalCenterArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文章ID */
    private Long articleId;

    /** 文章标题 */
    private String articleTitle;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
