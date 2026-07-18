<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Share, Download, Delete, Loading, Position, User, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

// Icons
import AIIcon from '@/assets/images/svg/ai/ai.svg?component'
import AIAnswerIcon from '@/assets/images/svg/ai/ai_answer.svg?component'
import WebSearchIcon from '@/assets/images/svg/ai/websearch.svg?component'
import UploadIcon from '@/assets/images/svg/ai/upload_attachment.svg?component'

import { homePageChatStreamApi, getHomePageChatListApi, getChatContextDetailApi } from '@/api/ai'
import type { AiHomePageChatListVO } from '@/types/ai'
import { processSseStream } from '@/utils/sse'

const router = useRouter()

// State
const aiInput = ref('')
const isLoading = ref(false)
const sessionId = ref(Date.now().toString())
const searchKeyword = ref('')
const currentChatContextId = ref<number | null>(null)
const chatListLoading = ref(false)
const chatList = ref<AiHomePageChatListVO[]>([])
const currentChatTitle = ref('新对话')

const isNewChat = computed(() => !currentChatContextId.value && messageList.value.length === 1 && messageList.value[0]?.id === 'init')

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
    <p>你好！我是你的 aiforge 智能助手 👋</p>
    <p>有什么我可以帮你的吗？</p>
  </div>`
}

const messageList = ref<ChatMessage[]>([{ ...initMessage }])
const chatBodyRef = ref<HTMLElement | null>(null)

// Load chat list
const loadChatList = async () => {
  chatListLoading.value = true
  try {
    const res = await getHomePageChatListApi({ pageNum: 1, pageSize: 50 })
    if (res.code === 200 && res.data) {
      chatList.value = res.data.records || []
    }
  } catch (error) {
    console.error('Failed to load chat list:', error)
  } finally {
    chatListLoading.value = false
  }
}

// Load chat detail
const loadChatDetail = async (chatContextId: number, title: string) => {
  if (currentChatContextId.value === chatContextId) return
  currentChatContextId.value = chatContextId
  currentChatTitle.value = title || '对话'
  isLoading.value = true
  try {
    const res = await getChatContextDetailApi({ chatContextId })
    if (res.code === 200 && res.data) {
      if (res.data.length > 0) {
        sessionId.value = res.data[0]?.sessionId || Date.now().toString()
        messageList.value = res.data.map(item => ({
          id: item.chatContextId.toString(),
          role: item.role as 'user' | 'assistant',
          content: item.content
        }))
      }
      scrollToBottom()
    }
  } catch (error) {
    console.error('Failed to load chat detail:', error)
  } finally {
    isLoading.value = false
  }
}

const createNewChat = () => {
  currentChatContextId.value = null
  currentChatTitle.value = '新对话'
  sessionId.value = Date.now().toString()
  messageList.value = [{ ...initMessage }]
  loadChatList()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

const sendMsg = async () => {
  if (!aiInput.value.trim() || isLoading.value) return

  const userMsg = aiInput.value.trim()
  aiInput.value = ''

  if (!currentChatContextId.value && messageList.value.length <= 1) {
    currentChatTitle.value = userMsg
    if (messageList.value.length === 1 && messageList.value[0]?.id === 'init') {
      messageList.value = []
    }
  }

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

    // Refresh list
    if (!currentChatContextId.value) {
      loadChatList()
    }
  } catch (error) {
    console.error('Chat request error:', error)
    ElMessage.error('网络异常，请稍后重试')
    isLoading.value = false
  } finally {
    scrollToBottom()
  }
}

onMounted(() => {
  loadChatList()
})
</script>

<template>
  <el-row class="ai-agent-layout">
    <!-- Sidebar -->
    <el-col :span="4" class="sidebar">
      <!-- Brand -->
      <div class="brand">
        <AIIcon class="brand-icon" />
        <span class="brand-title">aiforge 智能助手</span>
      </div>

      <!-- New Chat Btn -->
      <div class="new-chat-wrap">
        <el-button type="primary" class="new-chat-btn" @click="createNewChat">
          <el-icon><Plus /></el-icon> 新建对话
        </el-button>
      </div>

      <!-- Chat History -->
      <div class="history-section">
        <div class="history-header">
          <span class="history-title">对话历史</span>
          <el-icon class="search-icon"><Search /></el-icon>
        </div>

        <div class="history-list" v-loading="chatListLoading">
          <div
            v-for="item in chatList"
            :key="item.chatContextId"
            class="history-item"
            :class="{ active: currentChatContextId === item.chatContextId }"
            @click="loadChatDetail(item.chatContextId, item.content)"
          >
            <div class="item-content">
              <span class="item-text">{{ item.content }}</span>
            </div>
          </div>
          <div v-if="chatList.length === 0 && !chatListLoading" class="empty-list">暂无历史记录</div>
        </div>
      </div>

      <!-- User Profile -->
      <div class="user-profile">
<!--        <div class="avatar-wrap">-->
<!--          <el-icon><User /></el-icon>-->
<!--        </div>-->
<!--        <div class="user-info">-->
<!--          <span class="username">用户名</span>-->
<!--          <span class="tag">专业版</span>-->
<!--        </div>-->
<!--        <el-icon class="profile-more"><ArrowDown /></el-icon>-->
      </div>
    </el-col>

    <!-- Main Content -->
    <el-col :span="20" class="main-content" :class="{ 'is-new-chat': isNewChat }">
      <!-- Header -->
      <div class="main-header" v-if="!isNewChat">
        <div class="header-title">
          {{ currentChatTitle }}
          <el-icon class="edit-icon"><EditPen /></el-icon>
        </div>
        <div class="header-actions">
          <div class="action-btn"><el-icon><Share /></el-icon> 分享</div>
          <div class="action-btn"><el-icon><Download /></el-icon> 导出</div>
          <div class="action-btn"><el-icon><Delete /></el-icon></div>
        </div>
      </div>

      <!-- Chat Body -->
      <div class="chat-body" ref="chatBodyRef">
        <div
          v-for="msg in messageList"
          :key="msg.id"
          :class="['message-row', msg.role === 'user' ? 'row-user' : 'row-ai']"
        >
          <div class="avatar" v-if="msg.role === 'assistant'">
            <AIAnswerIcon class="avatar-icon ai-avatar-icon" />
          </div>

          <div class="message-bubble" :class="{'md-bubble': !msg.isHtml}">
            <div v-if="msg.isHtml" v-html="msg.content"></div>
            <MdPreview v-else :modelValue="msg.content" />
          </div>

          <div class="avatar" v-if="msg.role === 'user'">
            <div class="user-avatar"><el-icon><User /></el-icon></div>
          </div>
        </div>

        <div v-if="isLoading" class="message-row row-ai">
           <div class="avatar">
            <AIAnswerIcon class="avatar-icon ai-avatar-icon" />
          </div>
          <div class="message-bubble loading-bubble">
            <el-icon class="is-loading"><Loading /></el-icon> 正在思考中...
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div class="input-container">
          <textarea
            v-model="aiInput"
            class="chat-textarea"
            placeholder="输入你的问题... (Shift + Enter 换行，Enter 发送)"
            @keydown.enter.prevent="sendMsg"
          ></textarea>

          <div class="input-toolbar">
            <div class="toolbar-left">
              <div class="tool-btn">
                <WebSearchIcon class="tool-icon" /> 联网搜索
              </div>
              <div class="tool-btn">
                <UploadIcon class="tool-icon" /> 上传文件
              </div>
            </div>
            <div class="toolbar-right">
              <div class="send-btn" @click="sendMsg" :class="{ 'is-active': aiInput.trim().length > 0 }">
                <el-icon><Position /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<style scoped>
.ai-agent-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f7f8fa;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  background-color: #f7f8fa;
  border-right: 1px solid #eef0f4;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.brand {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  gap: 12px;
}

.brand-icon {
  width: 28px;
  height: 28px;
  color: #2b65f6;
  transform: rotateY(180deg);
}

.brand-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.new-chat-wrap {
  padding: 0 20px 15px;
}

.new-chat-btn {
  width: 100%;
  height: 40px;
  background-color: #2b65f6;
  border-color: #2b65f6;
  border-radius: 6px;
  font-size: 14px;
}

.history-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 8px 12px;
  color: #1a1a1a;
  font-size: 14px;
  font-weight: 500;
}

.search-icon {
  color: #8c9096;
  cursor: pointer;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: #eef0f4;
}

.history-item.active {
  background-color: #e4ebfb;
}

.item-content {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.item-text {
  font-size: 13px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-list {
  text-align: center;
  color: #8c9096;
  font-size: 13px;
  margin-top: 30px;
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #eef0f4;
  gap: 12px;
  cursor: pointer;
}

.avatar-wrap {
  width: 32px;
  height: 32px;
  background-color: #e4e7ed;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
}

.tag {
  background-color: #e4ebfb;
  color: #2b65f6;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.profile-more {
  color: #8c9096;
}

/* Main Content */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  height: 100%;
  overflow: hidden;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid #eef0f4;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-icon {
  color: #8c9096;
  cursor: pointer;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #5c5f66;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #1a1a1a;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px 15% 0;
  display: flex;
  flex-direction: column;
}

.message-row {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.row-ai {
  justify-content: flex-start;
}

.row-user {
  justify-content: flex-end;
}

.avatar {
  flex-shrink: 0;
}

.ai-avatar-icon {
  width: 36px;
  height: 36px;
  background-color: #2b65f6;
  border-radius: 50%;
  color: white;
  padding: 6px;
  box-sizing: border-box;
  transform: rotateY(180deg);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background-color: #2b65f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.message-bubble {
  max-width: 80%;
  border-radius: 8px;
  font-size: 15px;
  line-height: 1.6;
}

.row-ai .message-bubble {
  background-color: transparent;
  color: #1a1a1a;
}

.row-user .message-bubble {
  background-color: #f2f3f5;
  color: #1a1a1a;
  border-top-right-radius: 0;
}

:deep(.md-bubble .md-editor-preview) {
  background-color: transparent !important;
  color: inherit;
  font-size: 15px;
  line-height: 1.6;
}

:deep(.md-bubble .md-editor-preview-wrapper) {
  padding: 0 !important;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #5c5f66;
}

/* Input Area */
.input-area {
  padding: 20px 15% 40px;
}

.input-container {
  background-color: #ffffff;
  border: 1px solid #eef0f4;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-container:focus-within {
  border-color: #dcdfe6;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.chat-textarea {
  width: 100%;
  border: none;
  resize: none;
  padding: 12px 20px;
  font-size: 15px;
  line-height: 1.5;
  color: #1a1a1a;
  outline: none;
  min-height: 54px;
  background: transparent;
  font-family: inherit;
}

.chat-textarea::placeholder {
  color: #b4b8bf;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background-color: #fafbfc;
  border-top: 1px solid #f2f3f5;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #5c5f66;
  background-color: #ffffff;
  border: 1px solid #eef0f4;
  padding: 6px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-btn:hover {
  background-color: #f2f3f5;
}


.tool-icon {
  width: 14px;
  height: 14px;
}

.send-btn {
  width: 36px;
  height: 36px;
  background-color: #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 18px;
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

/* New Chat Center Layout */
.main-content.is-new-chat .chat-body {
  flex: 0 1 auto;
  margin-top: auto;
  align-items: center;
  padding: 0 15%;
}

.main-content.is-new-chat .input-area {
  margin-bottom: auto;
}

.main-content.is-new-chat .message-row {
  justify-content: center;
  width: 100%;
  margin-bottom: 40px;
}

.main-content.is-new-chat .avatar {
  display: none;
}

.main-content.is-new-chat .message-bubble {
  background: transparent;
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  max-width: 100%;
}

.main-content.is-new-chat .message-bubble p {
  margin: 12px 0;
}

.main-content.is-new-chat .chat-textarea {
  min-height: 80px;
  padding: 16px 20px;
}
</style>
