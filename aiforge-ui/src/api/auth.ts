import request from '@/utils/request'
import type { AuthParams, LoginData, ApiResponse } from '@/types/auth'

export const loginApi = (data: AuthParams) => {
    return request.post<any, ApiResponse<LoginData>>('/auth/login', data)
}

export const registerApi = (data: AuthParams) => {
    return request.post<any, ApiResponse<void>>('/auth/register', data)
}