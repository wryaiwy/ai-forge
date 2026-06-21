/**
 * 处理 Server-Sent Events (SSE) Fetch 数据流
 * 
 * @param response fetch 返回的 Response 对象
 * @param onMessage 接收到每一段数据(拆解了 data: 之后)的回调
 * @param onError 发生错误时的回调
 * @param onComplete 流接收完毕时的回调
 */
export async function processSseStream(
  response: Response,
  onMessage: (content: string) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
) {
  try {
    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('Response body is not readable')
    }

    const decoder = new TextDecoder('utf-8')
    let done = false

    while (!done) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      if (value) {
        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        lines.forEach(line => {
          if (line.startsWith('data:')) {
            onMessage(line.substring(5))
          } else if (line.trim() !== '') {
            // 兼容有些环境直接返回纯文本的情况，跳过其他的标准事件前缀
            if (!line.startsWith('event:') && !line.startsWith('id:') && !line.startsWith('retry:')) {
              onMessage(line)
            }
          }
        })
      }
    }

    if (onComplete) {
      onComplete()
    }
  } catch (error) {
    if (onError) {
      onError(error)
    } else {
      console.error('SSE Stream Error:', error)
    }
  }
}
