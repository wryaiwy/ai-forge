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
 * 文章DTO
 */
export interface BizArticleDTO {
  articleTitle: string;
  articleTags: string;
  content: string;
  publishStatus: number;
}

