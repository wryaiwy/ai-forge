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

// 删除文章
export const deleteArticlesApi = (data: number[]) => {
  return request.delete<any, ApiResponse<void>>('/biz/article/delete', { data })
}

// 修改文章
export const updateArticleApi = (data: BizArticleDTO) => {
  return request.put<any, ApiResponse<void>>('/biz/article/update', data)
}


/**
 * 个人中心文章列表（分页）
 */
export const getPersonalCenterArticlesApi = (params?: PageQuery) => {
  return request.get<any, ApiResponse<PageResult<PersonalCenterArticleVO>>>('/biz/article/personal-center', {
    params
  })
}

/**
 * 生成文章摘要（SSE 流式）
 * @param articleId 文章ID
 * @param onMessage 收到每段文本时的回调
 * @param onDone 流结束时的回调
 * @param onError 出错时的回调
 * @returns AbortController 用于取消请求
 */
export const generateArticleSummaryStreamApi = (
  articleId: number,
  onMessage: (text: string) => void,
  onDone?: () => void,
  onError?: (err: Error) => void
) => {
  const controller = new AbortController()
  const token = localStorage.getItem('aiforge_token')

  fetch(`${import.meta.env.VITE_PROXY_TARGET}/biz/article/summary/${articleId}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const reader = response.body?.getReader()
    if (!reader) throw new Error('ReadableStream not supported')

    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      let eventEndIndex;
      while ((eventEndIndex = buffer.indexOf('\n\n')) >= 0) {
        const event = buffer.slice(0, eventEndIndex)
        buffer = buffer.slice(eventEndIndex + 2)

        const lines = event.split('\n')
        const payload = lines
          .filter(line => line.startsWith('data:'))
          .map(line => {
            const dataStr = line.slice(5)
            return dataStr.startsWith(' ') ? dataStr.slice(1) : dataStr
          })
          .join('\n')

        if (payload) {
          onMessage(payload)
        }
      }
    }

    // 处理流结束前可能的残余数据
    if (buffer) {
      const lines = buffer.split('\n')
      const payload = lines
        .filter(line => line.startsWith('data:'))
        .map(line => {
          const dataStr = line.slice(5)
          return dataStr.startsWith(' ') ? dataStr.slice(1) : dataStr
        })
        .join('\n')
      if (payload) {
        onMessage(payload)
      }
    }

    onDone?.()
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError?.(err)
    }
  })

  return controller
}

/**
 * 文章知识问答（SSE 流式）
 * @param articleId 文章ID
 * @param question 提问内容
 * @param onMessage 收到每段文本时的回调
 * @param onDone 流结束时的回调
 * @param onError 出错时的回调
 * @returns AbortController 用于取消请求
 */
export const generateArticleQAStreamApi = (
  articleId: number,
  question: string,
  onMessage: (text: string) => void,
  onDone?: () => void,
  onError?: (err: Error) => void
) => {
  const controller = new AbortController()
  const token = localStorage.getItem('aiforge_token')
  const url = new URL(`${import.meta.env.VITE_PROXY_TARGET}/biz/article/qa/${articleId}`)
  url.searchParams.set('question', question)

  fetch(url.toString(), {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const reader = response.body?.getReader()
    if (!reader) throw new Error('ReadableStream not supported')

    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      let eventEndIndex;
      while ((eventEndIndex = buffer.indexOf('\n\n')) >= 0) {
        const event = buffer.slice(0, eventEndIndex)
        buffer = buffer.slice(eventEndIndex + 2)

        const lines = event.split('\n')
        const payload = lines
          .filter(line => line.startsWith('data:'))
          .map(line => {
            const dataStr = line.slice(5)
            return dataStr.startsWith(' ') ? dataStr.slice(1) : dataStr
          })
          .join('\n')

        if (payload) {
          onMessage(payload)
        }
      }
    }

    if (buffer) {
      const lines = buffer.split('\n')
      const payload = lines
        .filter(line => line.startsWith('data:'))
        .map(line => {
          const dataStr = line.slice(5)
          return dataStr.startsWith(' ') ? dataStr.slice(1) : dataStr
        })
        .join('\n')
      if (payload) {
        onMessage(payload)
      }
    }

    onDone?.()
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError?.(err)
    }
  })

  return controller
}
