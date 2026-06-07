package com.aiforge.web.controller;

import com.aiforge.ai.dto.ToolExecuteDTO;
import com.aiforge.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 跨语言 Tool 调用网关
 */
@Tag(name = "工具执行网关")
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@Slf4j
public class ToolController {

    @Operation(summary = "执行工具")
    @PostMapping("/execute")
    public Result<Object> executeTool(@RequestBody @Valid ToolExecuteDTO dto) {
        log.info("接收到工具调用请求: toolName={}, args={}", dto.getToolName(), dto.getArguments());
        
        // 工具分发逻辑
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");
        mockResult.put("executedTool", dto.getToolName());
        
        return Result.success(mockResult);
    }
}
