<script setup lang="ts">
import { ref, nextTick } from 'vue'
import HomeAIIcon from '@/assets/images/svg/home/home_ai.svg?component'
import { Close, Minus, Position, ArrowRight, Loading } from '@element-plus/icons-vue'
import { homePageChatStreamApi } from '@/api/ai'
import { processSseStream } from '@/utils/sse'
import { ElMessage } from 'element-plus'

const isChatOpen = ref(false)
const aiInput = ref('')
const isLoading = ref(false)
const sessionId = ref(Date.now().toString())

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  isHtml?: boolean
}

const messageList = ref<ChatMessage[]>([
  {
    id: 'init',
    role: 'assistant',
    isHtml: true,
    content: `<div class="message-content">
      <p class="greeting">你好！我是你的 AI 助手 👋</p>
      <p style="margin-bottom: 8px;">我可以帮你：</p>
      <ul>
        <li><span class="list-icon">🧠</span> 解答技术问题</li>
        <li><span class="list-icon">📄</span> 推荐相关文章</li>
        <li><span class="list-icon">⚙️</span> 总结文章内容</li>
        <li><span class="list-icon">💡</span> 提供学习建议</li>
      </ul>
      <p class="ask-text">你想问什么呢？</p>
    </div>`
  }
])

const chatBodyRef = ref<HTMLElement | null>(null)

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

const toggleChat = () => {
  isChatOpen.value = !isChatOpen.value
}

const closeChat = () => {
  isChatOpen.value = false
}

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
      content: userMsg
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

    // 隐藏 loading，因为已经开始接收数据
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

const fillQuestion = (question: string) => {
  aiInput.value = question
}
</script>

<template>
  <div class="ai-chat-section">
    <!-- Chat Dialog -->
    <transition name="chat-fade">
      <div v-show="isChatOpen" class="ai-chat-dialog">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-left">
            <div class="robot-icon-wrap">
              <HomeAIIcon class="header-robot-icon" />
            </div>
            <div class="header-info">
              <span class="title">AI 助手</span>
              <span class="status"><span class="green-dot"></span>在线</span>
            </div>
          </div>
          <div class="header-actions">
            <el-icon class="action-btn" @click="toggleChat"><Minus /></el-icon>
            <el-icon class="action-btn" @click="closeChat"><Close /></el-icon>
          </div>
        </div>

        <!-- Body -->
        <div class="chat-body" ref="chatBodyRef">
          <div
            v-for="msg in messageList"
            :key="msg.id"
            :class="['chat-message', msg.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div v-if="msg.isHtml" v-html="msg.content"></div>
            <div v-else class="message-content">
              {{ msg.content }}
            </div>
          </div>

          <div v-if="isLoading" class="chat-message ai-message loading-message">
            <el-icon class="is-loading"><Loading /></el-icon> 正在思考中...
          </div>

          <div class="suggestion-section">
            <p class="suggestion-title">试着问我</p>
            <div class="suggestion-list">
              <div class="suggestion-item" @click="fillQuestion('什么是 GraphQL？它和 REST 有什么区别？')">
                <span>什么是 GraphQL？它和 REST 有什么区别？</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
              <div class="suggestion-item" @click="fillQuestion('推荐几篇关于 Kubernetes 入门的文章')">
                <span>推荐几篇关于 Kubernetes 入门的文章</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
              <div class="suggestion-item" @click="fillQuestion('如何提升后端接口的安全性？')">
                <span>如何提升后端接口的安全性？</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="chat-footer">
          <div class="input-wrapper">
            <input
              v-model="aiInput"
              type="text"
              placeholder="输入你的问题..."
              @keyup.enter="sendMsg"
            />
            <div class="send-btn" @click="sendMsg" :class="{ 'is-active': aiInput.trim().length > 0 }">
              <el-icon><Position /></el-icon>
            </div>
          </div>
          <div class="disclaimer">
            AI 生成的内容可能不完全准确，请自行判断
          </div>
        </div>
      </div>
    </transition>

    <!-- Floating Button -->
    <div class="ai-float-button" :class="{ 'is-open': isChatOpen }" @click="toggleChat">
      <HomeAIIcon class="ai-icon" />
<!--      <span class="red-dot"></span>-->
    </div>
  </div>
</template>

<style scoped>
.ai-chat-section {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.ai-float-button {
  width: 60px;
  height: 60px;
  background-color: #ffffff;
  color: #909399;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  position: relative;
  transition: all 0.3s;
}

.ai-float-button:hover {
  transform: scale(1.05);
  background-color: #ecf5ff;
  color: #79bbff;
  border-color: #c6e2ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.ai-float-button.is-open {
  background-color: #409eff;
  color: #ffffff;
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.ai-icon {
  width: 32px;
  height: 32px;
  color: inherit;
}

.red-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background-color: #f56c6c;
  border-radius: 50%;
  border: 2px solid white;
}

.ai-chat-dialog {
  width: 380px;
  height: 600px;
  max-height: calc(100vh - 120px);
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.robot-icon-wrap {
  width: 36px;
  height: 36px;
  background-color: #e6f1fc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.header-robot-icon {
  width: 20px;
  height: 20px;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.header-info .title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-info .status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

.green-dot {
  width: 6px;
  height: 6px;
  background-color: #67c23a;
  border-radius: 50%;
}

.header-actions {
  display: flex;
  gap: 12px;
  color: #909399;
}

.action-btn {
  cursor: pointer;
  font-size: 16px;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #409eff;
}

.chat-body {
  padding: 20px;
  background-color: #fafbfc;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.chat-message {
  margin-bottom: 24px;
  width: 90%;
  align-self: center;
  word-break: break-word;
}

.user-message {
  background-color: #409eff;
  color: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
}

.ai-message {
  background-color: #f0f7ff;
  border-radius: 12px;
  padding: 16px;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.greeting {
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 15px;
}

.ai-message ul {
  list-style: none;
  padding: 0;
  margin: 0 0 12px 0;
}

.ai-message li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
}

.list-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background-color: #e6f1fc;
  border-radius: 50%;
  font-size: 12px;
}

.ask-text {
  color: #909399;
  font-size: 13px;
}

.suggestion-section {
  margin-top: 10px;
}

.suggestion-title {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 12px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.suggestion-item {
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 13px;
  color: #606266;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.suggestion-item:hover {
  border-color: #409eff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.chat-footer {
  padding: 16px 20px;
  background-color: #ffffff;
  border-top: 1px solid #f0f2f5;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 20px;
  padding: 8px 16px;
  margin-bottom: 12px;
  border: 1px solid transparent;
  transition: border-color 0.3s;
}

.input-wrapper:focus-within {
  border-color: #c6e2ff;
  background-color: #ffffff;
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: #303133;
}

.input-wrapper input::placeholder {
  color: #a8abb2;
}

.send-btn {
  color: #a8abb2;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 18px;
  transition: color 0.3s;
  margin-left: 8px;
}

.send-btn.is-active,
.send-btn:hover {
  color: #409eff;
}

.disclaimer {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}

.chat-fade-enter-active,
.chat-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: bottom right;
}

.chat-fade-enter-from,
.chat-fade-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}
</style>
