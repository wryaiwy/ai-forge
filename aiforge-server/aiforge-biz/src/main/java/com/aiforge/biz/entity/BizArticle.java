package com.aiforge.biz.entity;

import com.aiforge.biz.enums.ArticleStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_article")
public class BizArticle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文章ID */
    @TableId(type = IdType.AUTO)
    private Long articleId;

    /** 文章标题 */
    private String articleTitle;

    /** 文章标签 */
    private String articleTags;

    /** 文章内容 */
    private String content;

    /** 文章作者ID */
    private Long authorId;

    /** 文章发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 文章状态 */
    private Integer articleStatus;
}

