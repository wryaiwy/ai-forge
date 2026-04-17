// 请求参数：对应后端的 userName 和 password
export interface AuthParams {
    userName: string;
    password: string;
    confirmPassword?: string;
}

// 登录成功后的响应数据：对应后端的 Map<String, String>
export interface LoginData {
    token: string;
}

// 通用的 Result 封装结构
export interface ApiResponse<T = void> {
    code: number;
    msg: string;
    data: T;
}