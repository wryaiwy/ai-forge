package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiHomePageChatDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.vo.AiChatContextVO;
import com.aiforge.ai.vo.AiHomePageChatListVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Flux;
import java.util.List;

/**
 * AI对话消息Service接口
 */
public interface AiChatContextService extends IService<AiChatContext> {

    /**
     * 首页 AI 对话并入库（流式输出）
     *
     * @param addDTO 请求体
     * @return AI 回答流
     */
    Flux<String> homePageChat(AiHomePageChatDTO addDTO);

    /**
     * 删除对话消息（逻辑删除）
     * @param chatContextId 主键ID
     * @return 是否删除成功
     */
    boolean deleteChatContext(Long chatContextId);

    /**
     * 首页 AI对话助手 消息列表（按时间倒序，分页）
     * @param page 分页参数
     * @return 消息列表
     */
    IPage<AiHomePageChatListVO> getHomePageChatList(Page<AiChatContext> page);

    /**
     * 首页 AI对话助手 消息详情（根据chatContextId获取同sessionId下所有消息）
     * @param chatContextId 主键ID
     * @return 会话消息列表
     */
    List<AiChatContextVO> getChatContextDetail(Long chatContextId);
}