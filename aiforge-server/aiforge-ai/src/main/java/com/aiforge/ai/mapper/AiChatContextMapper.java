package com.aiforge.ai.mapper;

import com.aiforge.ai.entity.AiChatContext;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话消息Mapper接口
 */
@Mapper
public interface AiChatContextMapper extends BaseMapper<AiChatContext> {
}