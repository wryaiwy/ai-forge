package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiPolishDTO;
import com.aiforge.ai.dto.AiTranslateDTO;
import com.aiforge.ai.vo.AiPolishVO;
import com.aiforge.ai.vo.AiTranslateVO;

/**
 * @Description: AI 服务接口
 */
public interface AiService {

    /**
     * 文章润色
     *
     * @param polishDTO 润色请求参数
     * @return 润色结果 (含原始内容和润色后内容)
     */
    AiPolishVO polish(AiPolishDTO polishDTO);

    /**
     * 文章翻译
     *
     * @param translateDTO 翻译请求参数
     * @return 翻译结果 (含原始内容和翻译后内容)
     */
    AiTranslateVO translate(AiTranslateDTO translateDTO);
}
