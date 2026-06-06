package com.aiforge.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "全局搜索结果VO")
public class SearchResultVO {
    
    @Schema(description = "业务ID(如文章ID)")
    private Long id;
    
    @Schema(description = "业务类型(如article)")
    private String type;
    
    @Schema(description = "高亮后的标题")
    private String title;
    
    @Schema(description = "高亮后的内容摘要")
    private String contentSnippet;
}
