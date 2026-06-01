package com.aiforge.biz.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SeckillOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐ID */
    private Long packageId;
}
