package com.aiforge.biz.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillPackageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐名称 */
    private String name;

    /** 包含的 Token 数量 */
    private Integer tokenAmount;

    /** 秒杀价格 */
    private BigDecimal price;

    /** 总库存 */
    private Integer totalStock;

    /** 秒杀开始时间 */
    private LocalDateTime startTime;

    /** 秒杀结束时间 */
    private LocalDateTime endTime;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;
}
