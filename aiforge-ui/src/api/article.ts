import request from '@/utils/request'
import type {
  HomeArticleVO,
  BizArticleDTO,
  BizArticleVO,
  PersonalCenterArticleVO,
  PageQuery,
  PageResult,
} from '@/types/article'
import type { ApiResponse } from '@/types/auth'

/**
 * 获取最新文章列表
 * @param limit 返回的文章数量
 * @returns 最新文章列表
 */
export const getLatestArticlesApi = (limit: number = 6) => {
  return request.get<any, ApiResponse<HomeArticleVO[]>>('/biz/article/latest', {
    params: { limit }
  })
}

/**
 * 添加文章
 * @param data 文章数据
 * @returns 添加结果
 */
export const addArticleApi = (data: BizArticleDTO) => {
  return request.post<any, ApiResponse<void>>('/biz/article/add', data)
}

/**
 * 获取文章详情
 * @param articleId 文章ID
 */
export const getArticleDetailApi = (articleId: number) => {
  return request.get<any, ApiResponse<BizArticleVO>>(`/biz/article/detail/${articleId}`)
}

/**
 * 个人中心文章列表（分页）
 */
export const getPersonalCenterArticlesApi = (params?: PageQuery) => {
  return request.get<any, ApiResponse<PageResult<PersonalCenterArticleVO>>>('/biz/article/personal-center', {
    params
  })
}
