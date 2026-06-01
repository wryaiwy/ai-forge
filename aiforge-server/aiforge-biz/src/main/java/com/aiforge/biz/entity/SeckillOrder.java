package com.aiforge.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_seckill_order")
public class SeckillOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 订单主键 */
    @TableId(type = IdType.AUTO)
    private Long seckillOrderId;

    /** 订单编号 (全局唯一) */
    private String orderNo;

    /** 购买用户ID */
    private Long userId;

    /** 套餐ID */
    private Long packageId;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 订单状态: 0-未支付, 1-已支付, 2-已取消 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
}
