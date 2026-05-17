package com.aiforge.ai.listener;

import com.aiforge.ai.dto.KnowledgeDocDTO;
import com.aiforge.ai.service.AgentService;
import com.aiforge.common.event.KnowledgeAddEvent;
import com.aiforge.common.event.KnowledgeDeleteEvent;
import com.aiforge.common.event.KnowledgeUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeEventListener {

    private final AgentService agentService;

    @Async
    @EventListener
    public void handleKnowledgeAddEvent(KnowledgeAddEvent event) {
        log.info("接收到知识入库事件，开始异步处理向量化, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            KnowledgeDocDTO docDTO = new KnowledgeDocDTO();
            docDTO.setBizId(event.getBizId());
            docDTO.setBizType(event.getBizType());
            docDTO.setTitle(event.getTitle());
            docDTO.setContent(event.getContent());

            agentService.addKnowledge(docDTO);

            log.info("向量化处理完成, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化调用 Py 端失败, bizId: {}", event.getBizId(), e);
        }
    }

    @Async
    @EventListener
    public void handleKnowledgeUpdateEvent(KnowledgeUpdateEvent event) {
        log.info("接收到知识更新事件，开始异步处理向量化更新, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            // 先删除旧向量
            agentService.deleteKnowledge(event.getBizId(), event.getBizType());

            // 后添加新向量
            KnowledgeDocDTO docDTO = new KnowledgeDocDTO();
            docDTO.setBizId(event.getBizId());
            docDTO.setBizType(event.getBizType());
            docDTO.setTitle(event.getTitle());
            docDTO.setContent(event.getContent());

            agentService.addKnowledge(docDTO);

            log.info("向量化更新处理完成, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化更新调用 Py 端失败, bizId: {}", event.getBizId(), e);
        }
    }

    @Async
    @EventListener
    public void handleKnowledgeDeleteEvent(KnowledgeDeleteEvent event) {
        log.info("接收到知识删除事件，开始异步处理向量化删除, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            agentService.deleteKnowledge(event.getBizId(), event.getBizType());
            log.info("向量化删除处理完成, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化删除调用 Py 端失败, bizId: {}", event.getBizId(), e);
        }
    }
}
