package com.aiforge.biz.service.impl;

import com.aiforge.biz.entity.SeckillOrder;
import com.aiforge.biz.mapper.BizSeckillOrderMapper;
import com.aiforge.biz.mapper.BizSeckillPackageMapper;
import com.aiforge.biz.service.BizSeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizSeckillOrderServiceImpl extends ServiceImpl<BizSeckillOrderMapper, SeckillOrder> implements BizSeckillOrderService {

    private final BizSeckillPackageMapper bizSeckillPackageMapper;

}
