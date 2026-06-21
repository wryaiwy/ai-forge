package com.aiforge.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI对话消息实体类
 */
@Data
@TableName("ai_chat_context")
public class AiChatContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long chatContextId;

    /** 会话ID(用于将同一场对话串联起来) */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 角色：user(用户), assistant(AI助手), system(系统提示词) */
    private String role;

    /** 消息的具体内容 */
    private String content;

    /** 该条消息的Token消耗量 */
    private Integer tokenCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0-正常，1-已删除） */
    @TableLogic
    private Integer isDeleted;
}