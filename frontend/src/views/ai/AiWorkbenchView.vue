<template>
  <div class="ai-workbench">
    <section class="surface panel">
      <h2>AI 智能助手</h2>
      <p>支持自然语言问答，优先走系统内白名单查询，再回退到大模型生成。</p>
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="示例：帮我查一下苹果的库存；生成昨天的销售报表；给出今天采购建议"
      />
      <el-divider />
      <div class="tips">
        <div>
          <h4>能力边界</h4>
          <p>助手不会直接执行自由 SQL，仅调用受控查询接口，降低误操作风险。</p>
        </div>
        <div>
          <h4>响应模式</h4>
          <p>默认流式输出，网络异常可切换普通模式快速重试。</p>
        </div>
      </div>
    </section>
    <AiChatPanel />
  </div>
</template>

<script setup>
import AiChatPanel from '@/components/AiChatPanel.vue'
</script>

<style scoped>
.ai-workbench {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
}

.panel {
  padding: 20px;
  align-self: start;
}

.panel h2 {
  margin: 0 0 8px;
}

.panel p {
  margin: 0 0 10px;
  color: var(--text-soft);
}

.tips {
  display: grid;
  gap: 10px;
}

.tips h4 {
  margin: 0 0 4px;
}

.tips p {
  margin: 0;
}

@media (max-width: 1100px) {
  .ai-workbench {
    grid-template-columns: 1fr;
  }
}
</style>
