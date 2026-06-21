package com.aiforge.ai.convert;

import com.aiforge.ai.dto.AiHomePageChatDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.vo.AiChatContextVO;
import com.aiforge.ai.vo.AiHomePageChatListVO;
import org.mapstruct.Mapper;

/**
 * AI对话消息转换器
 */
@Mapper(componentModel = "spring")
public interface AiChatContextConvert {

    AiChatContext toEntity(AiHomePageChatDTO dto);

    AiChatContextVO toVO(AiChatContext entity);

    AiHomePageChatListVO toListVO(AiChatContext entity);
}
