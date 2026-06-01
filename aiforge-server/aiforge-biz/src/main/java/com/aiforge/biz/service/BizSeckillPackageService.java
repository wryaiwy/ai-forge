package com.aiforge.biz.service;

import com.aiforge.biz.entity.SeckillPackage;
import com.aiforge.common.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BizSeckillPackageService extends IService<SeckillPackage> {

    /**
     * 执行秒杀下单（用户端）
     */
    Result<String> doSeckill(Long packageId, Long userId);
}
