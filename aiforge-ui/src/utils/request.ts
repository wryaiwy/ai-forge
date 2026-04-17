import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_PROXY_TARGET,
    timeout: 10000, // 请求超时时间：10秒
})

// 2. 请求拦截器 (Request Interceptor)
service.interceptors.request.use(
    (config) => {
        // 每次发送请求之前，尝试从本地获取 token
        const token = localStorage.getItem('aiforge_token')

        if (token) {
            // 将 token 放到请求头中，这里后端使用的是 Authorization: Bearer <token>
            config.headers['Authorization'] = `Bearer ${token}`
        }

        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 3. 响应拦截器 (Response Interceptor)
service.interceptors.response.use(
    (response) => {
        // response.data 就是 Java 后端返回的 Result 对象
        const res = response.data

        // 假设你的 ResultCodeEnum 成功状态码是 200
        if (res.code && res.code !== 200) {
            // 业务逻辑报错（例如账号密码错误），直接弹出后端返回的 msg
            ElMessage.error(res.msg || '业务处理失败')

            // 如果是 401 Token 过期，这里可以补充清除本地 Token 并跳转到登录页的逻辑
            // if (res.code === 401) { ... }

            // 抛出错误，中断后续的 .then() 执行
            return Promise.reject(new Error(res.msg || 'Error'))
        } else {
            // 状态码正常，直接将后端返回的 Result（ApiResponse）返回给组件层
            return response.data as any
        }
    },
    (error) => {
        // 网络层面报错处理（比如服务器宕机、404、500 等 HTTP 状态码错误）
        let msg = '网络请求异常'
        if (error.response) {
            switch (error.response.status) {
                case 404: msg = '接口地址未找到'; break;
                case 500: msg = '服务器内部错误'; break;
                default: msg = `请求失败: ${error.response.status}`;
            }
        }
        ElMessage.error(msg)
        return Promise.reject(error)
    }
)

export default service
