package com.aiforge.web.controller;

import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.aiforge.biz.convert.ArticleConvert;
import com.aiforge.biz.vo.BizArticleVO;

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
public class BizArticleController {

    private final BizArticleService articleService;
    private final ArticleConvert articleConvert;

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
     * @param article 文章信息
     * @return 操作结果
     */
    @Operation(summary = "新增文章")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody BizArticle article) {
        articleService.save(article);
        return Result.success();
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
        articleService.updateById(article);
        return Result.success();
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
        articleService.removeByIds(articleIds);
        return Result.success();
    }

}