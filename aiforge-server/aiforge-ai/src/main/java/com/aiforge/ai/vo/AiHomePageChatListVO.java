package com.aiforge.ai.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: wengrunyang
 * @Description: 首页 AI对话助手 消息列表VO
 * @DateTime: 2026/6/21 20:55
 **/
@Data
public class AiHomePageChatListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long chatContextId;

    /** 消息内容（作为展示标题） */
    private String content;
}
