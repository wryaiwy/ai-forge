package com.aiforge.biz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀消息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 秒杀套餐ID */
    private Long packageId;

    /** 秒杀订单号 */
    private String orderNo;
}
