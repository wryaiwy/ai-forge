package com.aiforge.web.controller;

import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.aiforge.common.controller.BaseController;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.biz.vo.HomeArticleVO;
import com.aiforge.biz.vo.PersonalCenterArticleVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Author: wengrunyang
 * @Description: 文章管理
 * @DateTime: 2026/4/25 17:18
 **/
@Tag(name = "文章管理")
@RestController
@RequestMapping("/biz/article")
@RequiredArgsConstructor
public class BizArticleController extends BaseController {

    private final BizArticleService articleService;

    /**
     * 文章列表
     *
     * @return 文章列表
     */
    @Operation(summary = "文章列表")
    @GetMapping("/list")
    public Result<IPage<BizArticle>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(articleService.page(new Page<>(current, size)));
    }

    /**
     * 新增文章
     *
     * @param articleDTO 文章信息
     * @return 操作结果
     */
    @Operation(summary = "新增文章")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody BizArticleDTO articleDTO) {
        return toResult(articleService.saveArticle(articleDTO));
    }

    /**
     * 查看文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    @Operation(summary = "查看文章详情")
    @GetMapping("/detail/{articleId}")
    public Result<BizArticleVO> detail(@PathVariable Long articleId) {
        BizArticleVO articleVO = articleService.getByArticleId(articleId);
        return Result.success(articleVO);
    }

    /**
     * 修改文章
     *
     * @param article 文章信息
     * @return 操作结果
     */
    @Operation(summary = "修改文章")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody BizArticle article) {
        return toResult(articleService.updateArticle(article));
    }

    /**
     * 批量删除文章
     *
     * @param articleIds 文章ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除文章")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestBody List<Long> articleIds) {
        return toResult(articleService.deleteArticles(articleIds));
    }

    /**
     * 首页最新文章
     *
     * @return 最新文章列表
     */
    @Operation(summary = "首页最新文章")
    @GetMapping("/latest")
    public Result<List<HomeArticleVO>> latest(
            @RequestParam(defaultValue = "6") int limit) {
        return Result.success(articleService.getLatestPublished(limit));
    }

    /**
     * 个人中心文章列表
     *
     * @return 个人中心文章列表
     */
    @Operation(summary = "个人中心文章列表")
    @GetMapping("/personal-center")
    public Result<IPage<PersonalCenterArticleVO>> personalCenterArticles(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(articleService.getPersonalCenterArticles(new Page<>(current, size)));
    }

    /**
     * 生成整篇文章的一键摘要（流式输出）
     *
     * @param articleId 文章ID
     * @return 摘要流内容
     */
    @Operation(summary = "生成文章一键摘要（流式输出）")
    @GetMapping(value = "/summary/{articleId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSummary(@PathVariable Long articleId) {
        return articleService.generateSummaryStream(articleId);
    }

    /**
     * 生成文章知识问答（流式输出）
     *
     * @param articleId 文章ID
     * @param question 提问问题
     * @return 问答流内容
     */
    @Operation(summary = "生成文章知识问答（流式输出）")
    @GetMapping(value = "/qa/{articleId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateQA(@PathVariable Long articleId, @RequestParam String question) {
        return articleService.generateQAStream(articleId, question);
    }

}