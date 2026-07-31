package com.aiforge.ai.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexState;
import co.elastic.clients.elasticsearch.indices.stats.IndicesStats;
import com.aiforge.ai.dto.AiVectorDocQueryDTO;
import com.aiforge.ai.dto.AiVectorDocUpdateDTO;
import com.aiforge.ai.service.AiVectorInspectService;
import com.aiforge.ai.vo.AiVectorDocVO;
import com.aiforge.ai.vo.AiVectorIndexStatusVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量数据库查看服务实现类
 * 直接查询 Elasticsearch 获取向量索引数据
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiVectorInspectServiceImpl implements AiVectorInspectService {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${aiforge.vector.index-name:aiforge_docs}")
    private String vectorIndexName;

    /**
     * 获取向量索引状态
     */
    @Override
    public AiVectorIndexStatusVO getIndexStatus() {
        AiVectorIndexStatusVO vo = new AiVectorIndexStatusVO();
        vo.setIndexName(vectorIndexName);

        try {
            // 1. 检查索引是否存在
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(vectorIndexName)))
                    .value();
            vo.setExists(exists);

            if (!exists) {
                vo.setDocCount(0L);
                vo.setDeletedCount(0L);
                vo.setStoreSizeBytes(0L);
                vo.setStoreSize("0b");
                vo.setPrimaryShards(0);
                vo.setReplicaShards(0);
                vo.setHealthStatus("UNKNOWN");
                return vo;
            }

            // 2. 获取索引统计信息
            var statsResponse = elasticsearchClient.indices().stats(s -> s.index(vectorIndexName));
            IndicesStats indexStats = statsResponse.indices().get(vectorIndexName);

            if (indexStats != null && indexStats.total() != null) {
                var docs = indexStats.total().docs();
                if (docs != null) {
                    vo.setDocCount(docs.count());
                    vo.setDeletedCount(docs.deleted());
                }

                var store = indexStats.total().store();
                if (store != null) {
                    vo.setStoreSizeBytes(store.sizeInBytes());
                    vo.setStoreSize(formatBytes(store.sizeInBytes()));
                }
            }

            // 3. 获取索引设置（分片信息）
            var settingsResponse = elasticsearchClient.indices().getSettings(s -> s.index(vectorIndexName));
            IndexState indexState = settingsResponse.get(vectorIndexName);
            if (indexState != null && indexState.settings() != null
                    && indexState.settings().index() != null) {
                var indexSettings = indexState.settings().index();
                if (indexSettings.numberOfShards() != null) {
                    vo.setPrimaryShards(Integer.parseInt(indexSettings.numberOfShards()));
                }
                if (indexSettings.numberOfReplicas() != null) {
                    vo.setReplicaShards(Integer.parseInt(indexSettings.numberOfReplicas()));
                }
            }

            // 4. 获取集群健康状态
            var healthResponse = elasticsearchClient.cluster().health(h -> h.index(vectorIndexName));
            HealthStatus status = healthResponse.status();
            vo.setHealthStatus(status.jsonValue());

        } catch (IOException e) {
            log.error("获取向量索引状态失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "获取向量索引状态失败");
        }

        return vo;
    }

    /**
     * 分页查询向量文档列表
     */
    @Override
    public IPage<AiVectorDocVO> listDocuments(AiVectorDocQueryDTO queryDTO) {
        Page<AiVectorDocVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        try {
            // 构建查询条件
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index(vectorIndexName)
                    .query(q -> q.bool(b -> {
                        // 文本关键词搜索
                        if (StringUtils.hasText(queryDTO.getKeyword())) {
                            b.must(m -> m.match(mt -> mt.field("text").query(queryDTO.getKeyword())));
                        }
                        // 业务类型过滤
                        if (StringUtils.hasText(queryDTO.getBizType())) {
                            b.filter(f -> f.term(t -> t.field("metadata.bizType.keyword").value(queryDTO.getBizType())));
                        }
                        // 业务ID过滤
                        if (StringUtils.hasText(queryDTO.getBizId())) {
                            b.filter(f -> f.term(t -> t.field("metadata.bizId.keyword").value(queryDTO.getBizId())));
                        }
                        // 知识库ID过滤
                        if (StringUtils.hasText(queryDTO.getKbId())) {
                            b.filter(f -> f.term(t -> t.field("metadata.kbId.keyword").value(queryDTO.getKbId())));
                        }
                        // 用户ID过滤（使用当前登录用户）
                        Long userId = SecurityUtils.getUserId();
                        if (userId != null && userId > 0) {
                            b.filter(f -> f.term(t -> t.field("metadata.userId.keyword").value(String.valueOf(userId))));
                        }
                        return b;
                    }))
                    .from((int) ((queryDTO.getPageNum() - 1) * queryDTO.getPageSize()))
                    .size(queryDTO.getPageSize())
                    // 不返回向量字段，减少数据传输
                    .source(src -> src.filter(f -> f.excludes("vector"))),
                    Map.class
            );

            // 总命中数
            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            page.setTotal(total);

            // 解析文档列表
            List<AiVectorDocVO> records = response.hits().hits().stream()
                    .map(this::hitToVo)
                    .toList();
            page.setRecords(records);

        } catch (IOException e) {
            log.error("查询向量文档列表失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "查询向量文档列表失败");
        }

        return page;
    }

    /**
     * 根据文档ID获取详情
     */
    @Override
    public AiVectorDocVO getDocumentById(String docId) {
        try {
            GetResponse<Map> response = elasticsearchClient.get(g -> g
                            .index(vectorIndexName)
                            .id(docId)
                            // 不返回向量字段
                            .sourceExcludes(List.of("vector")),
                    Map.class
            );

            if (!response.found()) {
                throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文档不存在");
            }

            AiVectorDocVO vo = new AiVectorDocVO();
            vo.setDocId(response.id());

            Map<String, Object> source = response.source();
            if (source != null) {
                vo.setText((String) source.get("text"));
                vo.setMetadata(getMetadataMap(source.get("metadata")));

                // 提取 metadata 中的常用字段
                Map<String, Object> meta = vo.getMetadata();
                if (meta != null) {
                    vo.setBizId(getStringValue(meta.get("bizId")));
                    vo.setBizType(getStringValue(meta.get("bizType")));
                    vo.setKbId(getStringValue(meta.get("kbId")));
                    vo.setTags(getStringValue(meta.get("tags")));

                    // 权限校验：检查文档是否属于当前用户
                    Long currentUserId = SecurityUtils.getUserId();
                    if (currentUserId != null && currentUserId > 0) {
                        String docUserId = getStringValue(meta.get("userId"));
                        if (docUserId != null && !docUserId.isEmpty()
                                && !docUserId.equals(String.valueOf(currentUserId))) {
                            throw new AiForgeException(ResultCodeEnum.FORBIDDEN.getCode(), "无权访问该文档");
                        }
                    }
                }

                // 计算向量维度（从 mapping 推断，此处返回 null 标记为"不返回"）
                vo.setVectorDims(null);
            }

            return vo;
        } catch (AiForgeException e) {
            throw e;
        } catch (IOException e) {
            log.error("获取向量文档详情失败, docId: {}: {}", docId, e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "获取向量文档详情失败");
        }
    }

    /**
     * 根据文档ID删除向量文档
     */
    @Override
    public boolean deleteDocument(String docId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(vectorIndexName)
                    .id(docId)
            );
            log.info("删除向量文档成功, docId: {}", docId);
            return true;
        } catch (IOException e) {
            log.error("删除向量文档失败, docId: {}: {}", docId, e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "删除向量文档失败");
        }
    }

    /**
     * 更新向量文档元数据
     * 采用合并策略：新传入的字段会覆盖旧字段，未传入的旧字段保留
     */
    @Override
    public AiVectorDocVO updateDocumentMetadata(AiVectorDocUpdateDTO updateDTO) {
        String docId = updateDTO.getDocId();
        try {
            // 1. 先获取现有文档
            GetResponse<Map> getResponse = elasticsearchClient.get(g -> g
                            .index(vectorIndexName)
                            .id(docId),
                    Map.class
            );

            if (!getResponse.found()) {
                throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文档不存在");
            }

            Map<String, Object> source = getResponse.source();
            if (source == null) {
                throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文档数据为空");
            }

            // 2. 权限校验
            Map<String, Object> existingMetadata = getMetadataMap(source.get("metadata"));
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId != null && currentUserId > 0) {
                String docUserId = getStringValue(existingMetadata.get("userId"));
                if (docUserId != null && !docUserId.isEmpty()
                        && !docUserId.equals(String.valueOf(currentUserId))) {
                    throw new AiForgeException(ResultCodeEnum.FORBIDDEN.getCode(), "无权修改该文档");
                }
            }

            // 3. 构建更新内容（合并元数据）
            Map<String, Object> updateDoc = new HashMap<>();

            // 更新文本内容（如果传入）
            if (StringUtils.hasText(updateDTO.getText())) {
                updateDoc.put("text", updateDTO.getText());
            }

            // 合并元数据：保留旧字段，覆盖新字段
            if (updateDTO.getMetadata() != null && !updateDTO.getMetadata().isEmpty()) {
                Map<String, Object> mergedMetadata = new HashMap<>(existingMetadata);
                mergedMetadata.putAll(updateDTO.getMetadata());
                updateDoc.put("metadata", mergedMetadata);
            }

            // 4. 执行更新
            if (!updateDoc.isEmpty()) {
                UpdateResponse<Map> updateResponse = elasticsearchClient.update(u -> u
                                .index(vectorIndexName)
                                .id(docId)
                                .doc(updateDoc),
                        Map.class
                );
                log.info("更新向量文档成功, docId: {}, result: {}", docId, updateResponse.result().jsonValue());
            }

            // 5. 返回更新后的文档
            return getDocumentById(docId);

        } catch (AiForgeException e) {
            throw e;
        } catch (IOException e) {
            log.error("更新向量文档失败, docId: {}: {}", docId, e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "更新向量文档失败");
        }
    }

    /**
     * 将 ES Hit 转换为 VO
     */
    @SuppressWarnings("unchecked")
    private AiVectorDocVO hitToVo(Hit<Map> hit) {
        AiVectorDocVO vo = new AiVectorDocVO();
        vo.setDocId(hit.id());

        Map<String, Object> source = hit.source();
        if (source != null) {
            vo.setText((String) source.get("text"));
            vo.setMetadata(getMetadataMap(source.get("metadata")));

            // 提取 metadata 中的常用字段
            Map<String, Object> meta = vo.getMetadata();
            if (meta != null) {
                vo.setBizId(getStringValue(meta.get("bizId")));
                vo.setBizType(getStringValue(meta.get("bizType")));
                vo.setKbId(getStringValue(meta.get("kbId")));
                vo.setTags(getStringValue(meta.get("tags")));
            }
        }

        return vo;
    }

    /**
     * 安全获取 metadata Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMetadataMap(Object metadata) {
        if (metadata instanceof Map) {
            return (Map<String, Object>) metadata;
        }
        return new HashMap<>();
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    /**
     * 格式化字节数为可读字符串
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "b";
        if (bytes < 1024 * 1024) return String.format("%.1fkb", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1fmb", bytes / (1024.0 * 1024));
        return String.format("%.1fgb", bytes / (1024.0 * 1024 * 1024));
    }
}
