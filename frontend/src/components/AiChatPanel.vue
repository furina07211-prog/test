<template>
  <section class="surface chat-shell">
    <header class="chat-header">
      <div>
        <h3>智能对话助手</h3>
        <p>支持库存查询、销售报表、采购建议等自然语言问答。</p>
      </div>
      <el-tag :type="streaming ? 'success' : 'info'">{{ streaming ? '流式响应中' : '待命' }}</el-tag>
    </header>

    <div class="quick-actions">
      <el-button size="small" @click="usePrompt('帮我查一下苹果的库存')">查苹果库存</el-button>
      <el-button size="small" @click="usePrompt('生成昨天的销售报表')">昨日报表</el-button>
      <el-button size="small" @click="usePrompt('给我今天的采购建议')">采购建议</el-button>
    </div>

    <div class="chat-log" ref="logRef">
      <div v-for="item in messages" :key="item.id" :class="['bubble', item.role]">
        <div class="content">{{ item.content }}</div>
      </div>
    </div>

    <div class="chat-form">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="3"
        placeholder="输入问题，例如：帮我查一下苹果的库存"
        @keydown.enter.exact.prevent="submit"
      />
      <div class="chat-actions">
        <el-switch v-model="useStream" inline-prompt active-text="流式" inactive-text="普通" />
        <el-button @click="stopStream" :disabled="!streaming">中断</el-button>
        <el-button type="primary" :loading="sending" @click="submit">发送</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/modules'

const STORAGE_KEY = 'fruit-ai-session-v1'

const logRef = ref()
const draft = ref('')
const messages = ref([])
const sending = ref(false)
const streaming = ref(false)
const useStream = ref(true)
const sessionId = ref(`sess-${Date.now()}`)
let abortController = null

function usePrompt(text) {
  draft.value = text
}

function pushMessage(role, content) {
  const item = { id: `${Date.now()}-${Math.random()}`, role, content }
  messages.value.push(item)
  scrollToBottom()
  return item
}

function scrollToBottom() {
  nextTick(() => {
    if (logRef.value) {
      logRef.value.scrollTop = logRef.value.scrollHeight
    }
  })
}

function saveSession() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ sessionId: sessionId.value, messages: messages.value }))
}

function loadSession() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed.messages)) messages.value = parsed.messages
    if (parsed.sessionId) sessionId.value = parsed.sessionId
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
}

async function submit() {
  const content = draft.value.trim()
  if (!content || sending.value) return

  pushMessage('user', content)
  draft.value = ''
  sending.value = true

  const assistant = pushMessage('assistant', '')

  try {
    if (!useStream.value) {
      const res = await aiApi.chat({ message: content, sessionId: sessionId.value, stream: false })
      assistant.content = res?.answer || '未获取到回复'
      saveSession()
      return
    }

    abortController = new AbortController()
    streaming.value = true

    await aiApi.streamChat(
      { message: content, sessionId: sessionId.value, stream: true },
      {
        signal: abortController.signal,
        onChunk: (chunk) => {
          assistant.content += chunk
          scrollToBottom()
        },
        onDone: () => {
          saveSession()
        },
        onError: (errorText) => {
          ElMessage.error(errorText || '流式响应异常')
        }
      }
    )
  } catch (error) {
    if (error?.name !== 'AbortError') {
      ElMessage.error(error?.message || '请求失败')
      if (!assistant.content) {
        assistant.content = '服务暂不可用，请稍后重试。'
      }
    }
  } finally {
    sending.value = false
    streaming.value = false
    abortController = null
    saveSession()
  }
}

function stopStream() {
  if (abortController) {
    abortController.abort()
    streaming.value = false
    sending.value = false
  }
}

onMounted(() => {
  loadSession()
  if (!messages.value.length) {
    messages.value = [{ id: 'init', role: 'assistant', content: '你好，我可以帮你查询库存、生成销售统计和采购建议。' }]
  }
})
</script>

<style scoped>
.chat-shell {
  display: grid;
  gap: 12px;
  padding: 20px;
  min-height: 640px;
}

.chat-header h3 {
  margin: 0;
}

.chat-header p {
  margin: 4px 0 0;
  color: var(--text-soft);
}

.chat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.quick-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chat-log {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fafafa;
  padding: 12px;
  height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bubble {
  max-width: 80%;
  padding: 10px 12px;
  border-radius: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.bubble.user {
  margin-left: auto;
  background: #dfe7ff;
}

.bubble.assistant {
  background: #ffffff;
  border: 1px solid #e6e6e6;
}

.chat-form {
  display: grid;
  gap: 10px;
}

.chat-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}
</style>
