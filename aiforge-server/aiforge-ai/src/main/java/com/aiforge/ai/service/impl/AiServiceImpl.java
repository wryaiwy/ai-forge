package com.aiforge.ai.service.impl;

import com.aiforge.ai.dto.AiPolishDTO;
import com.aiforge.ai.enums.AiPolishModeEnum;
import com.aiforge.ai.service.AiService;
import com.aiforge.ai.vo.AiPolishVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @Description: AI 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 文章润色
     */
    @Override
    public AiPolishVO polish(AiPolishDTO polishDTO) {
        AiPolishModeEnum mode = AiPolishModeEnum.getByCode(polishDTO.getPolishMode());
        if (mode == null) {
            throw new AiForgeException(ResultCodeEnum.PARAM_ERROR.getCode(), "不支持的润色模式");
        }

        String systemPrompt = buildSystemPrompt(mode, polishDTO.getTargetStyle());
        String userPrompt = "请润色以下 Markdown 文章内容，直接返回润色后的完整内容，不要添加任何额外解释：\n\n" + polishDTO.getContent();

        String polishedContent;
        try {
            ChatClient chatClient = chatClientBuilder.build();
            polishedContent = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 润色调用失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 润色服务调用失败，请稍后重试");
        }

        AiPolishVO vo = new AiPolishVO();
        vo.setOriginalContent(polishDTO.getContent());
        vo.setPolishedContent(polishedContent);
        vo.setPolishModeDesc(mode.getDesc());
        return vo;
    }

    /**
     * 构建系统提示
     */
    private String buildSystemPrompt(AiPolishModeEnum mode, String targetStyle) {
        return switch (mode) {
            case GRAMMAR_FIX -> "你是一名专业的技术文章语法校对专家。请修正文章中的语法错误、拼写错误、标点符号错误，保持原文的写作风格和结构不变。只修正明显的错误，不要大幅改动原文内容。保持 Markdown 格式不变。";
            case PROFESSIONAL_POLISH -> "你是一名资深的技术博客编辑。请对文章进行全面润色，包括：优化语句表达使其更加流畅专业、改善段落结构和逻辑衔接、增强技术描述的准确性、统一术语使用。保持 Markdown 格式和原文的核心观点不变。";
            case STYLE_TRANSFER -> {
                String styleDesc = (targetStyle != null && !targetStyle.isBlank())
                        ? targetStyle
                        : "简洁、专业、适合技术博客发布";
                yield "你是一名专业的写作风格转换专家。请将文章改写为以下风格：" + styleDesc + "。保持文章的核心内容和技术准确性不变，只调整语言风格和表达方式。保持 Markdown 格式不变。";
            }
        };
    }
}
