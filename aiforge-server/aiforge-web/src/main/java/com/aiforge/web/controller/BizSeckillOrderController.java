package com.aiforge.web.controller;

import com.aiforge.biz.service.BizSeckillOrderService;
import com.aiforge.common.controller.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "秒杀订单管理")
@RestController
@RequestMapping("/biz/seckill/order")
@RequiredArgsConstructor
public class BizSeckillOrderController extends BaseController {

    private final BizSeckillOrderService bizSeckillOrderService;

}
