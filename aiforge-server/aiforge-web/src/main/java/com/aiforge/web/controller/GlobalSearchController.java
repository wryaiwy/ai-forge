package com.aiforge.web.controller;

import com.aiforge.biz.service.GlobalSearchService;
import com.aiforge.biz.vo.SearchResultVO;
import com.aiforge.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "全局搜索")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @Operation(summary = "全站跨域搜索")
    @GetMapping("/global")
    public Result<List<SearchResultVO>> globalSearch(@RequestParam String keyword) {
        return Result.success(globalSearchService.globalSearch(keyword));
    }
}
