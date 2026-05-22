/**
 * 个人中心文章列表 VO
 */
export interface PersonalCenterArticleVO {
  articleId: number
  articleTitle: string
  publishTime: string
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  current?: number
  size?: number
}

/**
 * MyBatis-Plus 分页响应
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 首页文章VO
 */
export interface HomeArticleVO {
  articleId: number;
  articleTitle: string;
  articleTags: string;
  authorName: string;
  publishTime: string;
}

/**
 * 文章详情 VO
 */
export interface BizArticleVO {
  articleId: number
  articleTitle: string
  articleTags: string
  content: string
  authorId: number
  authorName: string
  publishTime: string
}

/**
 * 文章DTO
 */
export interface BizArticleDTO {
  articleTitle: string;
  articleTags: string;
  content: string;
  publishStatus: number;
}

