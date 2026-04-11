package com.aiforge.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @Author: wengrunyang
 * @Description: sys_user实体类
 * @DateTime: 2026/4/11 9:38
 **/
@Data
@TableName("sys_user")
public class SysUser {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String nickName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 状态:0-禁用,1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
