package com.aiforge.ai.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: AI 润色结果 VO
 */
@Data
public class AiPolishVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 原始内容 */
    private String originalContent;

    /** 润色后的内容 */
    private String polishedContent;

    /** 润色模式描述 */
    private String polishModeDesc;
}
