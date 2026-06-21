package com.aiforge.ai.service.impl;

import com.aiforge.ai.convert.AiChatContextConvert;
import com.aiforge.ai.dto.AiHomePageChatDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.mapper.AiChatContextMapper;
import com.aiforge.ai.service.AiChatContextService;
import com.aiforge.ai.vo.AiChatContextVO;
import com.aiforge.ai.vo.AiHomePageChatListVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI对话消息Service实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatContextServiceImpl extends ServiceImpl<AiChatContextMapper, AiChatContext>
        implements AiChatContextService {

    private final AiChatContextConvert aiChatContextConvert;
    private final WebClient agentWebClient;

    /**
     * 首页 AI 对话并入库（流式输出）
     */
    @Override
    public Flux<String> homePageChat(AiHomePageChatDTO addDTO) {
        // 1. 记录用户提问
        AiChatContext userCtx = aiChatContextConvert.toEntity(addDTO);
        // Long userId = SecurityUtils.getUserId();
        Long userId = 1L;
        userCtx.setUserId(userId);
        userCtx.setRole("user");
        this.save(userCtx);

        // 2. 调用大模型 (流式) - 转移给 Python 代理端处理
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user_id", String.valueOf(userId));
        requestBody.put("message", addDTO.getContent());
        requestBody.put("conversation_id", addDTO.getSessionId());

        Flux<String> flux = agentWebClient.post()
                .uri("/agent/home-page-chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class);

        // 3. 收集并记录AI回答
        StringBuffer fullResponse = new StringBuffer();
        return flux.doOnNext(chunk -> {
            if (chunk != null) {
                fullResponse.append(chunk);
            }
        }).doOnComplete(() -> {
            AiChatContext aiCtx = new AiChatContext();
            aiCtx.setSessionId(addDTO.getSessionId());
            aiCtx.setUserId(userId);
            aiCtx.setRole("assistant");
            aiCtx.setContent(fullResponse.toString());
            this.save(aiCtx);
        }).doOnError(e -> {
            log.error("AI 问答流调用失败: {}", e.getMessage(), e);
        });
    }

    /**
     * 删除对话消息
     */
    @Override
    public boolean deleteChatContext(Long chatContextId) {
        return this.removeById(chatContextId);
    }

    /**
     * 首页 AI对话助手 消息列表（按时间倒序，分页）
     * 每个sessionId取第一条role=user的消息作为展示标题
     */
    @Override
    public IPage<AiHomePageChatListVO> getHomePageChatList(Page<AiChatContext> page) {
        // Long userId = SecurityUtils.getUserId();
        Long userId = 1L;
        // 子查询：每个sessionId下第一条user消息的chatContextId
        String subSql = "SELECT MIN(chat_context_id) FROM ai_chat_context"
                + " WHERE user_id = " + userId + " AND role = 'user' AND is_deleted = 0"
                + " GROUP BY session_id";
        LambdaQueryWrapper<AiChatContext> wrapper = new LambdaQueryWrapper<AiChatContext>()
                .inSql(AiChatContext::getChatContextId, subSql)
                .orderByDesc(AiChatContext::getCreateTime);
        return this.page(page, wrapper).convert(aiChatContextConvert::toListVO);
    }

    /**
     * 首页 AI对话助手 消息详情（根据chatContextId获取同sessionId下所有消息）
     */
    @Override
    public List<AiChatContextVO> getChatContextDetail(Long chatContextId) {
        AiChatContext entity = this.getById(chatContextId);
        if (entity == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "消息不存在");
        }
        LambdaQueryWrapper<AiChatContext> wrapper = new LambdaQueryWrapper<AiChatContext>()
                .eq(AiChatContext::getSessionId, entity.getSessionId())
                .orderByAsc(AiChatContext::getCreateTime);
        List<AiChatContext> list = this.list(wrapper);
        return list.stream()
                .map(aiChatContextConvert::toVO)
                .collect(Collectors.toList());
    }
}