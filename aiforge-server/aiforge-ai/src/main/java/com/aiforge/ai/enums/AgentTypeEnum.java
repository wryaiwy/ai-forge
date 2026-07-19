package com.aiforge.ai.enums;

import lombok.Getter;

@Getter
public enum AgentTypeEnum {
    HOME_CHAT("home_chat", "首页AI对话助手"),
    DRAFT("draft", "AI写作"),
    CRITIC("critic", "AI审查");

    private final String value;
    private final String desc;

    AgentTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
