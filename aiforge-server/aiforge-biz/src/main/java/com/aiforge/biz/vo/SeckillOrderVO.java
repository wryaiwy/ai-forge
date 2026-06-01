package com.aiforge.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillOrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 订单主键 */
    private Long seckillOrderId;

    /** 订单编号 */
    private String orderNo;

    /** 购买用户ID */
    private Long userId;

    /** 套餐ID */
    private Long packageId;

    /** 套餐名称 */
    private String packageName;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 订单状态: 0-未支付, 1-已支付, 2-已取消 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
}
