package com.aiforge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 自定义 Spring 事件
 */
@Getter
public class KnowledgeAddEvent extends ApplicationEvent {

    // 业务Id
    private final String bizId;
    // 业务类型
    private final String bizType;
    // 业务标题
    private final String title;
    // 业务内容
    private final String content;

    // 构造方法
    public KnowledgeAddEvent(Object source, String bizId, String bizType, String title, String content) {
        super(source);
        this.bizId = bizId;
        this.bizType = bizType;
        this.title = title;
        this.content = content;
    }

}
