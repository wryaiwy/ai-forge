package com.aiforge.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: wengrunyang
 * @Description: AI对话消息 响应VO
 * @DateTime: 2026/6/21 20:58
 **/
@Data
public class AiChatContextVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long chatContextId;

    /** 会话ID */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 角色：user(用户), assistant(AI助手), system(系统提示词) */
    private String role;

    /** 消息内容 */
    private String content;

    /** Token消耗量 */
    private Integer tokenCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
