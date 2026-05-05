package com.aiforge.web.controller;

import com.aiforge.common.result.Result;

/**
 * @Description: 基础控制器类
 */
public class BaseController {

    /**
     * 转换为 Result 类型
     *
     * @param success 是否成功
     * @return Result 类型
     */
    protected Result<Void> toResult(boolean success) {
        return success ? Result.success() : Result.fail();
    }

    /**
     * 转换为 Result 类型
     *
     * @param affectedRows 受影响的行数
     * @return Result 类型
     */
    protected Result<Void> toResult(int affectedRows) {
        return affectedRows > 0 ? Result.success() : Result.fail();
    }
}
