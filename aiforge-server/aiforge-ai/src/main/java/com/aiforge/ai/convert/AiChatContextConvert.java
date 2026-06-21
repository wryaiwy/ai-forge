package com.aiforge.ai.convert;

import com.aiforge.ai.dto.AiChatContextAddDTO;
import com.aiforge.ai.entity.AiChatContext;
import org.mapstruct.Mapper;

/**
 * AI对话消息转换器
 */
@Mapper(componentModel = "spring")
public interface AiChatContextConvert {

    AiChatContext toEntity(AiChatContextAddDTO dto);
}
