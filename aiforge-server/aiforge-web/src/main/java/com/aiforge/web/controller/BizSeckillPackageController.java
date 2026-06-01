package com.aiforge.web.controller;

import com.aiforge.biz.service.BizSeckillPackageService;
import com.aiforge.common.controller.BaseController;
import com.aiforge.common.result.Result;
import com.aiforge.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "秒杀套餐管理")
@RestController
@RequestMapping("/biz/seckill/package")
@RequiredArgsConstructor
public class BizSeckillPackageController extends BaseController {

    private final BizSeckillPackageService bizSeckillPackageService;

    /**
     * 执行秒杀下单（用户端）
     * 
     * @param packageId 秒杀套餐ID
     * @return 秒杀订单ID
     */
    @Operation(summary = "执行秒杀下单（用户端）")
    @PostMapping("/doSeckill")
    public Result<String> doSeckill(@RequestParam Long packageId) {
        // 压测专用：模拟 1~10000 的随机用户 ID
        // Long userId = ThreadLocalRandom.current().nextLong(1, 10001);

        Long userId = SecurityUtils.getUserId();
        return bizSeckillPackageService.doSeckill(packageId, userId);
    }
}
