package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiVectorDocQueryDTO;
import com.aiforge.ai.dto.AiVectorDocUpdateDTO;
import com.aiforge.ai.vo.AiVectorDocVO;
import com.aiforge.ai.vo.AiVectorIndexStatusVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 向量数据库查看服务接口
 */
public interface AiVectorInspectService {

    /**
     * 获取向量索引状态
     *
     * @return 索引状态信息
     */
    AiVectorIndexStatusVO getIndexStatus();

    /**
     * 分页查询向量文档列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    IPage<AiVectorDocVO> listDocuments(AiVectorDocQueryDTO queryDTO);

    /**
     * 根据文档ID获取详情
     *
     * @param docId 文档ID
     * @return 文档详情
     */
    AiVectorDocVO getDocumentById(String docId);

    /**
     * 根据文档ID删除向量文档
     *
     * @param docId 文档ID
     * @return 是否删除成功
     */
    boolean deleteDocument(String docId);

    /**
     * 更新向量文档元数据
     * 采用合并策略：新传入的字段会覆盖旧字段，未传入的旧字段保留
     *
     * @param updateDTO 更新参数（包含文档ID和要更新的元数据）
     * @return 更新后的文档详情
     */
    AiVectorDocVO updateDocumentMetadata(AiVectorDocUpdateDTO updateDTO);
}
