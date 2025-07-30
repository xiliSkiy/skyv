<template>
  <div class="page-header mb-4">
    <div class="d-flex justify-between items-center">
      <div>
        <h2 class="page-title mb-1">{{ title }}</h2>
        <p v-if="subtitle" class="page-subtitle mb-0">{{ subtitle }}</p>
      </div>
      <div class="page-actions">
        <slot name="actions"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 0.25rem;
}

.page-subtitle {
  font-size: 0.875rem;
  color: var(--el-text-color-secondary);
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.mb-0 {
  margin-bottom: 0;
}

.d-flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}
</style> 