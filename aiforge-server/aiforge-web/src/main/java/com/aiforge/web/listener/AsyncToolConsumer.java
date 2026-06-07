package com.aiforge.web.listener;

import com.aiforge.ai.dto.ToolExecuteDTO;
import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.enums.ArticleStatusEnum;
import com.aiforge.biz.service.BizArticleService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @Description: 异步指令类 Tool 消费端 (RabbitMQ)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncToolConsumer {

    private final BizArticleService bizArticleService;

    @RabbitListener(queuesToDeclare = @org.springframework.amqp.rabbit.annotation.Queue(com.aiforge.common.constant.MqConstants.ASYNC_TOOL_QUEUE), ackMode = "MANUAL")
    public void consumeAsyncTool(ToolExecuteDTO dto, Message message, Channel channel) {
        log.info("MQ 接收到异步工具执行指令: toolName={}, args={}", dto.getToolName(), dto.getArguments());
        try {
            // SaveDraftTool 逻辑实现
            if ("save_draft_tool".equals(dto.getToolName())) {
                Map<String, Object> args = dto.getArguments();
                BizArticleDTO articleDTO = new BizArticleDTO();
                articleDTO.setArticleTitle((String) args.getOrDefault("title", "未命名草稿"));
                articleDTO.setContent((String) args.getOrDefault("content", ""));
                articleDTO.setArticleTags("Draft");
                articleDTO.setPublishStatus(ArticleStatusEnum.DRAFT.getCode());

                bizArticleService.saveArticle(articleDTO);
                log.info("SaveDraftTool: 已成功将草稿入库");
            } else {
                log.warn("未知的异步工具执行指令: {}", dto.getToolName());
            }
            
            // 手动 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("异步工具执行指令处理成功: {}", dto.getToolName());
        } catch (Exception e) {
            log.error("异步工具执行指令处理失败: {}", dto.getToolName(), e);
            try {
                // 异常时拒绝且不重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ex) {
                log.error("MQ 消息 Reject 失败", ex);
            }
        }
    }
}
