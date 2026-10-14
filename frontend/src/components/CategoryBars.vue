<script setup>
defineProps({
  categories: { type: Array, required: true },
  showRecommendations: { type: Boolean, default: false }
})
</script>

<template>
  <ul class="cats">
    <li v-for="c in categories" :key="c.id">
      <div class="row">
        <span class="name">{{ c.name }}</span>
        <span class="score" :style="{ color: c.status?.color }">{{ c.score ?? '–' }}</span>
      </div>
      <div class="bar">
        <div class="fill" :style="{ width: (c.score ?? 0) + '%', background: c.status?.color }"></div>
      </div>
      <div class="meta">
        <span v-if="c.status" class="tag" :style="{ color: c.status.color }">{{ c.status.label }}</span>
        <span>{{ c.answeredQuestions }}/{{ c.totalQuestions }} answered</span>
      </div>
      <p v-if="showRecommendations && c.recommendation" class="reco">{{ c.recommendation }}</p>
    </li>
  </ul>
</template>

<style scoped>
.cats { list-style: none; margin: 0; padding: 0; display: grid; gap: 14px; }
.row { display: flex; justify-content: space-between; align-items: baseline; }
.name { font-weight: 600; }
.score { font-weight: 700; font-variant-numeric: tabular-nums; }
.bar { height: 8px; border-radius: 99px; background: var(--track); margin: 6px 0 4px; overflow: hidden; }
.fill { height: 100%; border-radius: 99px; transition: width 0.45s ease, background 0.3s ease; }
.meta { display: flex; gap: 10px; font-size: 12px; color: var(--muted); }
.tag { font-weight: 600; }
.reco { margin: 6px 0 0; font-size: 14px; color: var(--ink-2); line-height: 1.5; }
</style>
