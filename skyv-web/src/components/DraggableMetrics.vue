<template>
  <draggable
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    item-key="id"
    handle=".drag-handle"
    animation="300"
    @end="$emit('end', $event)"
  >
    <template #item="{element}">
      <slot name="item" :element="element"></slot>
    </template>
  </draggable>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import draggable from 'vuedraggable'

const props = defineProps({
  modelValue: {
    type: Array,
    required: true
  }
})

defineEmits(['update:modelValue', 'end'])
</script> 