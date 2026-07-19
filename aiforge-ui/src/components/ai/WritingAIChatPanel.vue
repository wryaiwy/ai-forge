<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { Close, Position, Loading, MagicStick, Document, ArrowRight } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { homePageChatStreamApi } from '@/api/ai'
import { processSseStream } from '@/utils/sse'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply-content', content: string): void
}>()

const closePanel = () => {
  emit('update:visible', false)
}

const aiInput = ref('')
const isLoading = ref(false)
const sessionId = ref(Date.now().toString())
const chatBodyRef = ref<HTMLElement | null>(null)
const panelRef = ref<HTMLElement | null>(null)

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  isHtml?: boolean
}

const initMessage: ChatMessage = {
  id: 'init',
  role: 'assistant',
  isHtml: true,
  content: `<div class="message-content">
    <p class="greeting">你好！我是你的 AI 写作助手 ✍️</p>
    <p style="margin-bottom: 8px;">我可以帮你：</p>
    <ul>
      <li><span class="list-icon">📝</span> 生成文章大纲</li>
      <li><span class="list-icon">✨</span> 扩写或优化段落</li>
      <li><span class="list-icon">🔍</span> 提供技术细节和示例</li>
      <li><span class="list-icon">💡</span> 总结全文生成摘要</li>
    </ul>
    <p class="ask-text">你想写点什么呢？</p>
  </div>`
}

const messageList = ref<ChatMessage[]>([{ ...initMessage }])

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    scrollToBottom()
  }
})

const sendMsg = async () => {
  if (!aiInput.value.trim() || isLoading.value) return

  const userMsg = aiInput.value.trim()
  aiInput.value = ''

  messageList.value.push({
    id: Date.now().toString(),
    role: 'user',
    content: userMsg
  })
  scrollToBottom()

  isLoading.value = true
  try {
    const response = await homePageChatStreamApi({
      sessionId: sessionId.value,
      content: userMsg,
      agentType: 'draft'
    })

    if (!response.ok) {
      ElMessage.error('AI 暂时无法回答')
      isLoading.value = false
      return
    }

    const aiMsgId = Date.now().toString()
    messageList.value.push({
      id: aiMsgId,
      role: 'assistant',
      content: ''
    })

    isLoading.value = false

    await processSseStream(
      response,
      (content) => {
        const currentMsg = messageList.value.find(m => m.id === aiMsgId)
        if (currentMsg) {
          currentMsg.content += content
          scrollToBottom()
        }
      },
      (error) => {
        console.error('Chat stream error:', error)
        ElMessage.error('网络流异常，请稍后重试')
      }
    )
  } catch (error) {
    console.error('Chat request error:', error)
    ElMessage.error('网络异常，请稍后重试')
    isLoading.value = false
  } finally {
    scrollToBottom()
  }
}

const applyToEditor = (content: string) => {
  emit('apply-content', content)
}

</script>

<template>
  <div class="writing-ai-overlay" :class="{ 'is-open': visible }">
    <div class="writing-ai-panel" :class="{ 'is-open': visible }" ref="panelRef">
      <!-- Header -->
      <div class="panel-header">
        <div class="header-left">
          <div class="icon-wrap">
            <el-icon><MagicStick /></el-icon>
          </div>
          <span class="title">AI 写作助手</span>
        </div>
        <div class="header-actions">
          <el-icon class="close-btn" @click="closePanel"><Close /></el-icon>
        </div>
      </div>

      <!-- Body -->
      <div class="panel-body" ref="chatBodyRef">

        <div
          v-for="msg in messageList"
          :key="msg.id"
          :class="['chat-message', msg.role === 'user' ? 'user-message' : 'ai-message']"
        >
          <div v-if="msg.isHtml" v-html="msg.content"></div>
          <div v-else class="message-content-wrapper">
            <MdPreview :modelValue="msg.content" class="md-preview-override" />
            <div class="msg-actions" v-if="msg.role === 'assistant' && msg.content && !isLoading">
              <el-button size="small" type="primary" plain @click="applyToEditor(msg.content)">
                <el-icon style="margin-right: 4px"><Document /></el-icon> 插入到编辑器
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="isLoading" class="chat-message ai-message loading-message">
          <el-icon class="is-loading"><Loading /></el-icon> 正在思考中...
        </div>


      </div>

      <!-- Footer -->
      <div class="panel-footer">
        <div class="input-wrapper">
          <textarea
            v-model="aiInput"
            class="chat-textarea"
            placeholder="给 AI 助手发送指令..."
            @keydown.enter.prevent="sendMsg"
          ></textarea>
          <div class="toolbar">
            <div class="send-btn" @click="sendMsg" :class="{ 'is-active': aiInput.trim().length > 0 }">
              <el-icon><Position /></el-icon>
            </div>
          </div>
        </div>
        <div class="disclaimer">AI 生成内容仅供参考</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.writing-ai-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  pointer-events: none;
  background-color: transparent;
  transition: background-color 0.3s;
}

.writing-ai-overlay.is-open {
  /* Remove pointer-events: auto so clicks can pass through to the document and trigger click outside */
}

.writing-ai-panel {
  position: fixed;
  top: 152px;
  right: -500px;
  width: 420px;
  height: calc(100vh - 160px);
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #eef0f4;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
}

.writing-ai-panel.is-open {
  right: 2%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eef0f4;
  background-color: #ffffff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrap {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.close-btn {
  font-size: 20px;
  color: #8c9096;
  cursor: pointer;
  transition: color 0.2s;
}
.close-btn:hover {
  color: #1a1a1a;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fafbfc;
  display: flex;
  flex-direction: column;
}


.chat-message {
  margin-bottom: 24px;
  width: 100%;
  word-break: break-word;
}

.user-message {
  background-color: #409eff;
  color: #fff;
  border-radius: 12px;
  border-top-right-radius: 2px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  align-self: flex-end;
  max-width: 85%;
  margin-left: auto;
}

.ai-message {
  background-color: #ffffff;
  border: 1px solid #eef0f4;
  border-radius: 12px;
  border-top-left-radius: 2px;
  padding: 16px;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  align-self: flex-start;
  width: 100%;
  box-sizing: border-box;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-actions {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f2f3f5;
  padding-top: 12px;
  margin-top: 4px;
}

:deep(.md-preview-override) {
  background: transparent !important;
  color: inherit;
  font-size: 14px;
  line-height: 1.6;
  padding: 0 !important;
}

:deep(.message-content p.greeting) {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
}

:deep(.message-content ul) {
  list-style: none;
  padding: 0;
  margin: 0 0 16px 0;
}

:deep(.message-content li) {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #606266;
}

.list-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #e6f1fc;
  border-radius: 6px;
  font-size: 12px;
}

:deep(.message-content .ask-text) {
  color: #909399;
  font-size: 13px;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  border: none;
  background: transparent;
}



.panel-footer {
  padding: 16px 20px;
  background-color: #ffffff;
  border-top: 1px solid #eef0f4;
}

.input-wrapper {
  background-color: #f5f7fa;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: border-color 0.3s, background-color 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.input-wrapper:focus-within {
  border-color: #c6e2ff;
  background-color: #ffffff;
}

.chat-textarea {
  width: 100%;
  border: none;
  resize: none;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.5;
  color: #1a1a1a;
  outline: none;
  min-height: 60px;
  max-height: 150px;
  background: transparent;
  font-family: inherit;
  box-sizing: border-box;
}

.chat-textarea::placeholder {
  color: #a8abb2;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
}

.send-btn {
  width: 32px;
  height: 32px;
  background-color: #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 16px;
  cursor: not-allowed;
  transition: background-color 0.2s;
}

.send-btn.is-active {
  background-color: #2b65f6;
  cursor: pointer;
}

.send-btn.is-active:hover {
  background-color: #1a51d6;
}

.disclaimer {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  margin-top: 12px;
}
</style>
