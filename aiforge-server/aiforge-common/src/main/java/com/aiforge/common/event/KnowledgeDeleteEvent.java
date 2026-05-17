package com.aiforge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 知识删除事件
 */
@Getter
public class KnowledgeDeleteEvent extends ApplicationEvent {

    private final String bizId;
    private final String bizType;

    public KnowledgeDeleteEvent(Object source, String bizId, String bizType) {
        super(source);
        this.bizId = bizId;
        this.bizType = bizType;
    }
}
