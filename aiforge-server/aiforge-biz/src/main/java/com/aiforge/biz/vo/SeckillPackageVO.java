package com.aiforge.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillPackageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐ID */
    private Long packageId;

    /** 套餐名称 */
    private String name;

    /** 包含的 Token 数量 */
    private Integer tokenAmount;

    /** 秒杀价格 */
    private BigDecimal price;

    /** 总库存 */
    private Integer totalStock;

    /** 剩余可用库存 */
    private Integer availableStock;

    /** 秒杀开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 秒杀结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
