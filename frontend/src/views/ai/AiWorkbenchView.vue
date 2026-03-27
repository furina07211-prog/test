<template>
  <div class="ai-workbench">
    <section class="surface panel">
      <h2>智能助手</h2>
      <p>支持自然语言建单、库存风险查询和销售报表分析，写操作默认二次确认。</p>

      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="演示建议：先发起建单指令查看预览，再点击确认创建草稿单。"
      />

      <el-divider />

      <div class="tips">
        <div class="tip-block">
          <h4>能力范围</h4>
          <p>1. 生成采购/销售草稿单（需确认）</p>
          <p>2. 查询库存预警、临期商品、销量排行</p>
          <p>3. 生成销售报表并给出环比结论</p>
        </div>
        <div class="tip-block">
          <h4>安全机制</h4>
          <p>1. 所有写入动作必须手动确认</p>
          <p>2. 确认动作10分钟失效，防止误触发</p>
          <p>3. 历史会话支持本地与后端双记录</p>
        </div>
        <div class="tip-block">
          <h4>示例指令</h4>
          <p>1. 帮我生成一个向华南果业采购100斤苹果的采购单</p>
          <p>2. 查询当前库存预警和7天内临期商品</p>
          <p>3. 生成近7天销售报表并给分析结论</p>
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

.tip-block h4 {
  margin: 0 0 4px;
}

.tip-block p {
  margin: 0 0 4px;
}

@media (max-width: 1100px) {
  .ai-workbench {
    grid-template-columns: 1fr;
  }
}
</style>

