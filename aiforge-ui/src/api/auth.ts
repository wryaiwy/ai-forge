import request from '@/utils/request'
import type { AuthParams, LoginData, ApiResponse } from '@/types/auth'

/**
 * 登录接口
 * @param data 登录参数
 * @returns 登录结果
 */
export const loginApi = (data: AuthParams) => {
    return request.post<any, ApiResponse<LoginData>>('/auth/login', data)
}

/**
 * 注册接口
 * @param data 注册参数
 * @returns 注册结果
 */
export const registerApi = (data: AuthParams) => {
    return request.post<any, ApiResponse<void>>('/auth/register', data)
}