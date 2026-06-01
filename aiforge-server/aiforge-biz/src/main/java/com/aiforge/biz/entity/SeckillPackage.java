package com.aiforge.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_seckill_package")
public class SeckillPackage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐ID */
    @TableId(type = IdType.AUTO)
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

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
