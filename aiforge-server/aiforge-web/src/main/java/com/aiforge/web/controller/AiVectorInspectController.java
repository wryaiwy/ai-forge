package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiVectorDocQueryDTO;
import com.aiforge.ai.dto.AiVectorDocUpdateDTO;
import com.aiforge.ai.service.AiVectorInspectService;
import com.aiforge.ai.vo.AiVectorDocVO;
import com.aiforge.ai.vo.AiVectorIndexStatusVO;
import com.aiforge.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 向量数据库查看 Controller
 */
@Tag(name = "向量数据库管理")
@RestController
@RequestMapping("/ai/vector-store")
@RequiredArgsConstructor
public class AiVectorInspectController {

    private final AiVectorInspectService aiVectorInspectService;

    /**
     * 获取向量索引状态
     *
     * @return 索引状态信息
     */
    @Operation(summary = "获取向量索引状态")
    @GetMapping("/status")
    public Result<AiVectorIndexStatusVO> getIndexStatus() {
        return Result.success(aiVectorInspectService.getIndexStatus());
    }

    /**
     * 分页查询向量文档列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Operation(summary = "分页查询向量文档列表")
    @GetMapping("/documents")
    public Result<IPage<AiVectorDocVO>> listDocuments(@Valid AiVectorDocQueryDTO queryDTO) {
        return Result.success(aiVectorInspectService.listDocuments(queryDTO));
    }

    /**
     * 根据文档ID获取详情
     *
     * @param docId 文档ID
     * @return 文档详情
     */
    @Operation(summary = "获取向量文档详情")
    @GetMapping("/documents/{docId}")
    public Result<AiVectorDocVO> getDocumentById(@PathVariable String docId) {
        return Result.success(aiVectorInspectService.getDocumentById(docId));
    }

    /**
     * 根据文档ID删除向量文档
     *
     * @param docId 文档ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除向量文档")
    @DeleteMapping("/documents/{docId}")
    public Result<Boolean> deleteDocument(@PathVariable String docId) {
        return Result.success(aiVectorInspectService.deleteDocument(docId));
    }

    /**
     * 更新向量文档元数据
     *
     * @param updateDTO 更新参数
     * @return 更新后的文档详情
     */
    @Operation(summary = "更新向量文档元数据")
    @PutMapping("/documents")
    public Result<AiVectorDocVO> updateDocumentMetadata(@Valid @RequestBody AiVectorDocUpdateDTO updateDTO) {
        return Result.success(aiVectorInspectService.updateDocumentMetadata(updateDTO));
    }
}
