package com.aiforge.ai.mcp;

import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.enums.ArticleStatusEnum;
import com.aiforge.biz.mapper.BizArticleMapper;
import com.aiforge.system.entity.SysUser;
import com.aiforge.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户与个人业务 MCP 工具集（供 Python Agent 跨语言调用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMcpTools {

    private final SysUserMapper sysUserMapper;
    private final BizArticleMapper bizArticleMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "当用户询问'我是谁'、'我的个人信息'、'我的账号信息'、'我的用户名/昵称/邮箱/手机号是什么'时调用此工具获取当前登录用户的身份资料。")
    public String getWhoAmI(@ToolParam(description = "当前登录用户ID（由底层安全上下文自动注入）") Long userId) {
        log.info("【MCP Server】执行 getWhoAmI, userId={}", userId);
        if (userId == null) {
            return "未获取到当前登录用户ID，请先登录。";
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return "未找到该用户的个人资料。";
        }
        return String.format(
                "当前登录用户信息如下：\n- 用户ID: %d\n- 用户名: %s\n- 昵称: %s\n- 邮箱: %s\n- 手机号: %s\n- 账号状态: %s\n- 注册时间: %s",
                user.getUserId(),
                user.getUserName(),
                user.getNickName() != null ? user.getNickName() : "未设置",
                user.getEmail() != null ? user.getEmail() : "未绑定",
                user.getPhone() != null ? user.getPhone() : "未绑定",
                Integer.valueOf(1).equals(user.getStatus()) ? "正常" : "禁用",
                user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "未知"
        );
    }

    @Tool(description = "当用户询问'我发布了哪些文章'、'我写过什么文章'、'我的文章列表'、'我有几篇草稿/已发布文章'时调用此工具获取当前用户的文章列表。")
    public String getMyPublishedArticles(@ToolParam(description = "当前登录用户ID（由底层安全上下文自动注入）") Long userId) {
        log.info("【MCP Server】执行 getMyPublishedArticles, userId={}", userId);
        if (userId == null) {
            return "未获取到当前登录用户ID，请先登录。";
        }
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<BizArticle>()
                .select(
                        BizArticle::getArticleId,
                        BizArticle::getArticleTitle,
                        BizArticle::getArticleTags,
                        BizArticle::getPublishTime,
                        BizArticle::getArticleStatus
                )
                .eq(BizArticle::getAuthorId, userId)
                .orderByDesc(BizArticle::getPublishTime);

        List<BizArticle> articles = bizArticleMapper.selectList(wrapper);
        if (articles == null || articles.isEmpty()) {
            return "你目前还没有创作或发布过任何文章。";
        }

        String listStr = articles.stream()
                .map(a -> {
                    String statusDesc = ArticleStatusEnum.getDescByCode(a.getArticleStatus());
                    String pubTime = a.getPublishTime() != null ? a.getPublishTime().format(DATE_FORMATTER) : "未记录";
                    String tags = (a.getArticleTags() != null && !a.getArticleTags().isBlank()) ? a.getArticleTags() : "无标签";
                    return String.format("- 《%s》 (文章ID: %d | 状态: %s | 标签: %s | 时间: %s)",
                            a.getArticleTitle(),
                            a.getArticleId(),
                            statusDesc != null ? statusDesc : "未知",
                            tags,
                            pubTime);
                })
                .collect(Collectors.joining("\n"));

        return String.format("你共创作了 %d 篇文章：\n%s", articles.size(), listStr);
    }

    @Configuration
    public static class McpServerToolConfig {
        @Bean
        public ToolCallbackProvider userMcpToolCallbackProvider(UserMcpTools userMcpTools) {
            return MethodToolCallbackProvider.builder()
                    .toolObjects(userMcpTools)
                    .build();
        }
    }
}

