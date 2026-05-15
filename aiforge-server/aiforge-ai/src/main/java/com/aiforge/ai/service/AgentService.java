package com.aiforge.ai.service;

import com.aiforge.ai.dto.ChatAgentDTO;
import com.aiforge.ai.dto.KnowledgeDocDTO;
import com.aiforge.ai.dto.RagQueryDTO;
import com.aiforge.ai.vo.ChatAgentVO;
import com.aiforge.ai.vo.KnowledgeDocVO;
import com.aiforge.ai.vo.RagQueryVO;

/**
 * @Description: Agent 服务接口
 */
public interface AgentService {

    /**
     * 客服聊天
     * @param chatDTO 客服聊天消息DTO参数
     * @return 客服聊天消息VO
     */
    ChatAgentVO chat(ChatAgentDTO chatDTO);

    /**
     * 知识库查询
     * @param ragQueryDTO 知识库查询消息DTO参数
     * @return 知识库查询消息VO
     */
    RagQueryVO ragQuery(RagQueryDTO ragQueryDTO);

    /**
     * 添加知识文档
     * @param knowledgeDocDTO 知识文档消息DTO参数
     * @return 知识文档消息VO
     */
    KnowledgeDocVO addKnowledge(KnowledgeDocDTO knowledgeDocDTO);

    /**
     * 删除知识文档
     * @param docId 知识文档ID
     * @return 空结果
     */
    boolean deleteKnowledge(String docId);
}
