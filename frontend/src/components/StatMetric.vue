<template>
  <article class="metric surface">
    <div class="metric-head">
      <span>{{ label }}</span>
      <small>{{ hint }}</small>
    </div>
    <div class="metric-value">{{ value }}</div>
  </article>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  hint: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.metric {
  padding: 18px 20px;
  min-height: 132px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-head {
  display: flex;
  justify-content: space-between;
  color: var(--text-soft);
}

.metric-value {
  font-family: 'Poppins', sans-serif;
  font-size: 38px;
  font-weight: 700;
  line-height: 1;
}
</style>
