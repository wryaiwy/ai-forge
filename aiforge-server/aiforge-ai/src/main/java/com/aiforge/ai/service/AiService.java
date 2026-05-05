package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiPolishDTO;
import com.aiforge.ai.vo.AiPolishVO;

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
}
