package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
@Schema(description = "工具执行请求参数")
public class ToolExecuteDTO {

    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toolName;

    @Schema(description = "工具参数")
    private Map<String, Object> arguments;
}
