<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../lib/api'
import ScoreRing from '../components/ScoreRing.vue'
import CategoryBars from '../components/CategoryBars.vue'

const props = defineProps({ id: { type: String, required: true } })
const card = ref(null)
const error = ref(null)

onMounted(async () => {
  try {
    card.value = await api.scorecard(props.id)
  } catch (e) {
    error.value = e.message
  }
})

const t = computed(() => card.value?.template)
const r = computed(() => card.value?.result)
const date = computed(() => card.value && new Date(card.value.createdAt).toLocaleDateString(undefined, { dateStyle: 'long' }))
</script>

<template>
  <p v-if="error" class="alert">{{ error }}</p>

  <div v-if="card" class="result" :style="{ '--brand': t.branding.primaryColor }">
    <div class="toolbar">
      <RouterLink to="/" class="back">← All assessments</RouterLink>
      <div class="buttons">
        <RouterLink :to="`/templates/${t.id}`" class="btn">New assessment</RouterLink>
        <a :href="api.pdfUrl(card.id)" class="btn primary" download>Download PDF</a>
      </div>
    </div>

    <article class="sheet card">
      <header class="banner">
        <div>
          <div class="logo">{{ t.branding.logoText }}</div>
          <div class="tagline">{{ t.branding.tagline }}</div>
        </div>
        <div class="meta">
          <div>{{ t.branding.organization }}</div>
          <div>{{ date }}</div>
        </div>
      </header>

      <h1>{{ t.report.title }}</h1>
      <p class="intro">{{ t.report.intro }}</p>

      <div class="overall">
        <ScoreRing :score="r.overallScore" :color="r.status?.color" :size="150" />
        <div>
          <span class="pill" :style="{ background: r.status?.color }">{{ r.status?.label }}</span>
          <p class="summary">{{ r.status?.summary }}</p>
          <p class="who">
            Prepared for <strong>{{ card.respondentName }}</strong><template v-if="card.respondentOrg">, {{ card.respondentOrg }}</template>
            · {{ r.answeredQuestions }} of {{ r.totalQuestions }} scored questions answered
          </p>
        </div>
      </div>

      <CategoryBars :categories="r.categories" show-recommendations />

      <footer>{{ t.report.footer }}</footer>
    </article>
  </div>
</template>

<style scoped>
.result { max-width: 860px; margin: 0 auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; gap: 12px; flex-wrap: wrap; }
.back { color: var(--muted); text-decoration: none; font-size: 14px; }
.buttons { display: flex; gap: 10px; }
.sheet { padding: 0 0 24px; overflow: hidden; }
.sheet > :not(.banner) { margin-left: 32px; margin-right: 32px; }
.banner { display: flex; justify-content: space-between; align-items: center; background: var(--brand); color: #fff; padding: 22px 32px; }
.logo { font-size: 22px; font-weight: 800; }
.tagline { font-size: 13px; opacity: 0.85; }
.meta { text-align: right; font-size: 13px; line-height: 1.6; opacity: 0.95; }
h1 { margin-top: 26px; margin-bottom: 6px; font-size: 28px; letter-spacing: -0.02em; }
.intro { color: var(--ink-2); line-height: 1.6; margin-top: 0; }
.overall { display: flex; gap: 24px; align-items: center; margin-top: 18px; margin-bottom: 26px; }
.pill { display: inline-block; color: #fff; font-weight: 600; padding: 5px 12px; border-radius: 99px; }
.summary { color: var(--ink-2); line-height: 1.55; margin: 10px 0 6px; }
.who { color: var(--muted); font-size: 14px; margin: 0; }
footer { margin-top: 26px; padding-top: 14px; border-top: 1px solid var(--line); font-size: 13px; color: var(--muted); }
@media (max-width: 640px) {
  .overall { flex-direction: column; align-items: flex-start; }
  .sheet > :not(.banner) { margin-left: 18px; margin-right: 18px; }
  .banner { padding: 18px; }
}
</style>
