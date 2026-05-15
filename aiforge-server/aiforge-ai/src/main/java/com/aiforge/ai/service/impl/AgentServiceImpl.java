package com.aiforge.ai.service.impl;

import com.aiforge.ai.dto.ChatAgentDTO;
import com.aiforge.ai.dto.KnowledgeDocDTO;
import com.aiforge.ai.dto.RagQueryDTO;
import com.aiforge.ai.service.AgentService;
import com.aiforge.ai.vo.ChatAgentVO;
import com.aiforge.ai.vo.KnowledgeDocVO;
import com.aiforge.ai.vo.RagQueryVO;
import com.aiforge.ai.vo.RagSourceVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Agent 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final WebClient agentWebClient;

    /**
     * 客服聊天
     */
    @Override
    public ChatAgentVO chat(ChatAgentDTO chatDTO) {
        try {
            String responseBody = agentWebClient.post()
                    .uri("/agent/chat")
                    .bodyValue(chatDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = parseResponse(responseBody);
            JsonNode data = root.get("data");

            ChatAgentVO vo = new ChatAgentVO();
            vo.setAnswer(data.get("answer").asText());
            vo.setConversationId(data.get("conversation_id").asText());
            if (data.has("usage") && !data.get("usage").isNull()) {
                vo.setUsage(jsonToMap(data.get("usage")));
            }
            return vo;
        } catch (AiForgeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Agent 客服聊天接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 客服服务调用失败，请稍后重试");
        }
    }

    /**
     * 知识库查询
     */
    @Override
    public RagQueryVO ragQuery(RagQueryDTO ragQueryDTO) {
        try {
            String responseBody = agentWebClient.post()
                    .uri("/agent/rag/query")
                    .bodyValue(ragQueryDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = parseResponse(responseBody);
            JsonNode data = root.get("data");

            RagQueryVO vo = new RagQueryVO();
            vo.setAnswer(data.get("answer").asText());
            vo.setSources(jsonToSourceList(data.get("sources")));
            return vo;
        } catch (AiForgeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Agent RAG 查询接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 知识库查询失败，请稍后重试");
        }
    }

    /**
     * 添加知识文档
     */
    @Override
    public KnowledgeDocVO addKnowledge(KnowledgeDocDTO knowledgeDocDTO) {
        try {
            String responseBody = agentWebClient.post()
                    .uri("/agent/knowledge/add")
                    .bodyValue(knowledgeDocDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = parseResponse(responseBody);
            JsonNode data = root.get("data");

            KnowledgeDocVO vo = new KnowledgeDocVO();
            vo.setDocId(data.get("doc_id").asText());
            vo.setStatus(data.get("status").asText());
            return vo;
        } catch (Exception e) {
            log.error("调用 Agent 知识库入库接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "知识文档入库失败，请稍后重试");
        }
    }

    /**
     * 删除知识文档
     */
    @Override
    public boolean deleteKnowledge(String docId) {
        try {
            String responseBody = agentWebClient.post()
                    .uri("/agent/knowledge/delete")
                    .bodyValue(buildDeleteRequest(docId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = parseResponse(responseBody);
            return root.get("code").asInt() == 200;
        } catch (Exception e) {
            log.error("调用 Agent 知识库删除接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "知识文档删除失败，请稍后重试");
        }
    }

    /**
     * 解析 Agent 服务响应
     */
    private JsonNode parseResponse(String responseBody) {
        try {
            // TODO 这里需要把局部 new 替换为 Spring 管理的单例 Bean。
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            int code = root.get("code").asInt();
            if (code != 200) {
                String message = root.get("message").asText();
                throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), message);
            }
            return root;
        } catch (AiForgeException e) {
            throw e;
        } catch (Exception e) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "Agent 服务响应解析失败");
        }
    }

    /**
     * 解析 JSON 节点为 Map
     */
    private Map<String, Object> jsonToMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        // 解析JSON树
        node.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            if (value.isTextual()) {
                map.put(entry.getKey(), value.asText());  // 字符串类型
            } else if (value.isNumber()) {
                map.put(entry.getKey(), value.numberValue()); // 数字类型
            } else if (value.isBoolean()) {
                map.put(entry.getKey(), value.asBoolean());  // 布尔
            } else {
                map.put(entry.getKey(), value.toString());      // 其他
            }
        });
        return map;
    }

    /**
     * 解析 JSON 节点为 RAG 源列表
     */
    private List<RagSourceVO> jsonToSourceList(JsonNode sourcesNode) {
        List<RagSourceVO> list = new ArrayList<>();
        if (sourcesNode == null || !sourcesNode.isArray()) {
            return list;
        }
        for (JsonNode item : sourcesNode) {
            RagSourceVO source = new RagSourceVO();
            source.setContent(item.has("content") ? item.get("content").asText() : "");
            source.setScore(item.has("score") ? item.get("score").asDouble() : 0.0);
            if (item.has("metadata") && !item.get("metadata").isNull()) {
                source.setMetadata(jsonToMap(item.get("metadata")));
            }
            list.add(source);
        }
        return list;
    }

    /**
     * 构建删除知识文档请求参数
     */
    private Map<String, String> buildDeleteRequest(String docId) {
        Map<String, String> req = new HashMap<>();
        req.put("doc_id", docId);
        return req;
    }
}
