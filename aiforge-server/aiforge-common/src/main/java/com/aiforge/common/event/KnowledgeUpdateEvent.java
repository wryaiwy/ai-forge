package com.aiforge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 知识更新事件
 */
@Getter
public class KnowledgeUpdateEvent extends ApplicationEvent {

    private final String bizId;
    private final String bizType;
    private final String title;
    private final String content;

    public KnowledgeUpdateEvent(Object source, String bizId, String bizType, String title, String content) {
        super(source);
        this.bizId = bizId;
        this.bizType = bizType;
        this.title = title;
        this.content = content;
    }
}
