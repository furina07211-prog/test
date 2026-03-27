<template>
  <section class="surface chat-shell">
    <header class="chat-header">
      <div>
        <h3>智能对话助手</h3>
        <p>支持建单预览确认、库存查询、销量排行和销售报表分析。</p>
      </div>
      <div class="header-actions">
        <el-tag :type="streaming ? 'success' : sending ? 'warning' : 'info'">
          {{ streaming ? '流式响应中' : sending ? '处理中' : '待命' }}
        </el-tag>
        <el-button text size="small" @click="clearLocalSession">清空本地会话</el-button>
      </div>
    </header>

    <div class="quick-actions">
      <div class="quick-group">
        <span>建单类</span>
        <el-button size="small" @click="usePrompt('帮我生成一个向华南果业采购100斤苹果的采购单')">建采购单</el-button>
        <el-button size="small" @click="usePrompt('帮我生成一个给优鲜门店销售80斤香蕉的销售单')">建销售单</el-button>
      </div>
      <div class="quick-group">
        <span>查询类</span>
        <el-button size="small" @click="usePrompt('查询当前库存预警')">查预警</el-button>
        <el-button size="small" @click="usePrompt('查询7天内临期商品')">查临期</el-button>
        <el-button size="small" @click="usePrompt('给我近30天销量TOP5')">查排行</el-button>
      </div>
      <div class="quick-group">
        <span>报表类</span>
        <el-button size="small" @click="usePrompt('生成近7天销售报表并给分析结论')">近7天报表</el-button>
        <el-button size="small" @click="usePrompt('生成昨天销售报表')">昨日报表</el-button>
      </div>
    </div>

    <div class="chat-log" ref="logRef" v-loading="historyLoading">
      <div v-for="item in messages" :key="item.id" :class="['bubble', item.role]">
        <template v-if="item.kind === 'confirm'">
          <div class="content">{{ item.content }}</div>
          <el-descriptions :column="1" border size="small" class="confirm-desc">
            <el-descriptions-item label="单据类型">{{ item.preview?.bizType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="业务对象">{{ item.preview?.partnerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ item.preview?.warehouseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="水果">{{ item.preview?.fruitName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ item.preview?.quantityKg ?? '-' }} kg</el-descriptions-item>
            <el-descriptions-item label="单价">{{ item.preview?.unitPrice ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="预计金额">{{ item.preview?.estimatedAmount ?? '-' }}</el-descriptions-item>
          </el-descriptions>
          <div class="confirm-actions">
            <el-button
              type="primary"
              size="small"
              :loading="item.pending"
              :disabled="item.resolved"
              @click="confirmAction(item, true)"
            >
              确认创建
            </el-button>
            <el-button
              size="small"
              :loading="item.pending"
              :disabled="item.resolved"
              @click="confirmAction(item, false)"
            >
              取消
            </el-button>
            <el-tag v-if="item.resolved" type="info">{{ item.resolvedText || '已处理' }}</el-tag>
          </div>
        </template>
        <template v-else>
          <div class="content">{{ item.content }}</div>
        </template>
      </div>
    </div>

    <div class="chat-form">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="3"
        placeholder="输入问题，例如：帮我生成一个向华南果业采购100斤苹果的采购单"
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

const STORAGE_KEY = 'fruit-ai-session-v2'

const logRef = ref()
const draft = ref('')
const messages = ref([])
const sending = ref(false)
const streaming = ref(false)
const useStream = ref(true)
const historyLoading = ref(false)
const sessionId = ref(`sess-${Date.now()}`)

let abortController = null

function createId() {
  return `${Date.now()}-${Math.random()}`
}

function usePrompt(text) {
  draft.value = text
}

function pushMessage(role, content, extra = {}) {
  const item = { id: createId(), role, content, kind: 'text', ...extra }
  messages.value.push(item)
  scrollToBottom()
  return item
}

function pushConfirmMessage(dispatchResult) {
  const item = {
    id: createId(),
    role: 'assistant',
    kind: 'confirm',
    content: '以下为草稿预览，确认后才会写入业务数据。',
    actionId: dispatchResult.actionId,
    preview: dispatchResult.preview || {},
    pending: false,
    resolved: false,
    resolvedText: ''
  }
  messages.value.push(item)
  scrollToBottom()
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

function loadLocalSession() {
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

async function loadServerHistory() {
  historyLoading.value = true
  try {
    const page = await aiApi.assistantHistory({ sessionId: sessionId.value, pageNo: 1, pageSize: 200 })
    const records = Array.isArray(page?.records) ? page.records : []
    if (!records.length) return

    const exists = new Set(messages.value.map((msg) => `${msg.role}|${msg.content}`))
    for (const row of records) {
      const role = row.role === 'user' ? 'user' : 'assistant'
      const content = row.content || ''
      const key = `${role}|${content}`
      if (!exists.has(key)) {
        messages.value.push({
          id: createId(),
          role,
          content,
          kind: 'text',
          createTime: row.createTime,
          intentCode: row.intentCode,
          toolName: row.toolName
        })
        exists.add(key)
      }
    }
    scrollToBottom()
  } catch (error) {
    ElMessage.warning(error?.message || '历史记录加载失败')
  } finally {
    historyLoading.value = false
  }
}

function buildClarificationText(items) {
  if (!Array.isArray(items) || !items.length) return ''
  return `请先补充以下信息：\n${items.map((text, index) => `${index + 1}. ${text}`).join('\n')}`
}

async function fallbackChat(content) {
  if (!useStream.value) {
    const res = await aiApi.chat({ message: content, sessionId: sessionId.value, stream: false })
    pushMessage('assistant', res?.answer || '未获取到回复')
    return
  }

  const assistant = pushMessage('assistant', '')
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
      onError: (errorText) => {
        ElMessage.error(errorText || '流式响应异常')
      }
    }
  )
}

async function submit() {
  const content = draft.value.trim()
  if (!content || sending.value) return

  pushMessage('user', content)
  draft.value = ''
  sending.value = true

  try {
    const dispatchRes = await aiApi.assistantDispatch({ message: content, sessionId: sessionId.value })
    if (dispatchRes?.matched) {
      if (dispatchRes.answer) {
        pushMessage('assistant', dispatchRes.answer, { intentCode: dispatchRes.intentCode })
      }
      const clarificationText = buildClarificationText(dispatchRes.clarifications)
      if (clarificationText) {
        pushMessage('assistant', clarificationText, { intentCode: dispatchRes.intentCode })
      }
      if (dispatchRes.requiresConfirm && dispatchRes.actionId) {
        pushConfirmMessage(dispatchRes)
      }
      saveSession()
      return
    }

    await fallbackChat(content)
  } catch (error) {
    if (error?.name !== 'AbortError') {
      ElMessage.error(error?.message || '请求失败')
      pushMessage('assistant', '请求失败，请稍后重试。')
    }
  } finally {
    sending.value = false
    streaming.value = false
    abortController = null
    saveSession()
  }
}

async function confirmAction(item, confirm) {
  if (item.pending || item.resolved) return
  item.pending = true
  try {
    const res = await aiApi.assistantConfirm({
      actionId: item.actionId,
      confirm,
      sessionId: sessionId.value
    })
    item.resolved = true
    item.resolvedText = confirm ? '已确认' : '已取消'
    pushMessage('assistant', res?.answer || '操作完成。')
  } catch (error) {
    ElMessage.error(error?.message || '确认操作失败')
    if (error?.message?.includes('失效') || error?.message?.includes('超时')) {
      item.resolved = true
      item.resolvedText = '已过期'
    }
  } finally {
    item.pending = false
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

function clearLocalSession() {
  localStorage.removeItem(STORAGE_KEY)
  sessionId.value = `sess-${Date.now()}`
  messages.value = [{ id: 'init', role: 'assistant', kind: 'text', content: '会话已重置。你可以开始新的业务指令。' }]
  saveSession()
}

onMounted(async () => {
  loadLocalSession()
  if (!messages.value.length) {
    messages.value = [{ id: 'init', role: 'assistant', kind: 'text', content: '你好，我可以帮你建采购单/销售单、查预警、查临期、查销量排行并生成销售报表。' }]
  }
  await loadServerHistory()
  saveSession()
})
</script>

<style scoped>
.chat-shell {
  display: grid;
  gap: 12px;
  padding: 20px;
  min-height: 640px;
}

.chat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chat-header h3 {
  margin: 0;
}

.chat-header p {
  margin: 4px 0 0;
  color: var(--text-soft);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-actions {
  display: grid;
  gap: 8px;
}

.quick-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-group span {
  color: var(--text-soft);
  font-size: 12px;
  width: 52px;
}

.chat-log {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fafafa;
  padding: 12px;
  height: 440px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bubble {
  max-width: 88%;
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

.confirm-desc {
  margin-top: 10px;
}

.confirm-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
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

