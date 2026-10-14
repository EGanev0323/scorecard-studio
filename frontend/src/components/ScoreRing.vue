<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: { type: Number, default: null },
  color: { type: String, default: '#cbd5e1' },
  size: { type: Number, default: 132 }
})

const radius = 52
const circumference = 2 * Math.PI * radius
const dash = computed(() => (props.score == null ? 0 : (props.score / 100) * circumference))
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 120 120" class="ring" role="img"
       :aria-label="score == null ? 'No score yet' : `Score ${score} out of 100`">
    <circle cx="60" cy="60" :r="radius" fill="none" stroke="var(--track)" stroke-width="10" />
    <circle v-if="dash > 0" cx="60" cy="60" :r="radius" fill="none" :stroke="color" stroke-width="10" stroke-linecap="round"
            :stroke-dasharray="`${dash} ${circumference}`" transform="rotate(-90 60 60)" class="arc" />
    <text x="60" y="62" text-anchor="middle" class="value">{{ score ?? '–' }}</text>
    <text x="60" y="80" text-anchor="middle" class="max">out of 100</text>
  </svg>
</template>

<style scoped>
.ring { flex: none; }
.arc { transition: stroke-dasharray 0.45s ease, stroke 0.3s ease; }
.value { font-size: 30px; font-weight: 700; fill: var(--ink); }
.max { font-size: 9px; fill: var(--muted); letter-spacing: 0.04em; }
</style>
