package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import com.aiforge.ai.vo.AiArticlePolishVO;
import com.aiforge.ai.vo.AiArticleTranslateVO;

/**
 * @Description: AI 服务接口
 */
public interface AiArticleService {

    /**
     * 文章润色
     *
     * @param polishDTO 润色请求参数
     * @return 润色结果 (含原始内容和润色后内容)
     */
    AiArticlePolishVO polish(AiArticlePolishDTO polishDTO);

    /**
     * 文章翻译
     *
     * @param translateDTO 翻译请求参数
     * @return 翻译结果 (含原始内容和翻译后内容)
     */
    AiArticleTranslateVO translate(AiArticleTranslateDTO translateDTO);
}
