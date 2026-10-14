<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../lib/api'

const templates = ref([])
const recent = ref([])
const error = ref(null)

onMounted(async () => {
  try {
    ;[templates.value, recent.value] = await Promise.all([api.templates(), api.recent(8)])
  } catch (e) {
    error.value = e.message
  }
})

const fmt = (iso) => new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
</script>

<template>
  <section class="hero">
    <h1>Turn questionnaires into branded scorecards</h1>
    <p>
      Pick an assessment, answer the questions and watch the score update live. Every question, scoring rule,
      threshold and brand color comes from a JSON template, so a new assessment is a new config file, not new code.
    </p>
  </section>

  <p v-if="error" class="alert">{{ error }}</p>

  <h2 class="section-title">Assessments</h2>
  <div class="grid">
    <RouterLink v-for="t in templates" :key="t.id" :to="`/templates/${t.id}`" class="card tpl"
                :style="{ '--brand': t.branding.primaryColor }">
      <div class="tpl-head">
        <span class="logo">{{ t.branding.logoText }}</span>
        <span class="version">v{{ t.version }}</span>
      </div>
      <h3>{{ t.name }}</h3>
      <p>{{ t.description }}</p>
      <div class="tpl-foot">
        <span>{{ t.categoryCount }} categories · {{ t.questionCount }} questions</span>
        <span class="go">Start →</span>
      </div>
    </RouterLink>
  </div>

  <h2 class="section-title">Recent scorecards</h2>
  <div v-if="recent.length" class="card list">
    <RouterLink v-for="s in recent" :key="s.id" :to="`/scorecards/${s.id}`" class="list-row">
      <span class="score-badge" :style="{ background: s.statusColor }">{{ s.overallScore ?? '–' }}</span>
      <span class="who">
        <strong>{{ s.respondentName }}</strong>
        <small>{{ s.respondentOrg || '—' }}</small>
      </span>
      <span class="what">{{ s.templateName }}</span>
      <span class="status" :style="{ color: s.statusColor }">{{ s.statusLabel }}</span>
      <span class="when">{{ fmt(s.createdAt) }}</span>
    </RouterLink>
  </div>
  <p v-else class="empty">No scorecards yet. Start one of the assessments above.</p>
</template>

<style scoped>
.hero { padding: 24px 0 8px; max-width: 760px; }
.hero h1 { font-size: 34px; line-height: 1.15; margin: 0 0 12px; letter-spacing: -0.02em; }
.hero p { color: var(--ink-2); font-size: 17px; line-height: 1.6; margin: 0; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 18px; }
.tpl { display: flex; flex-direction: column; gap: 6px; color: inherit; text-decoration: none;
  border-top: 5px solid var(--brand); transition: transform 0.15s ease, box-shadow 0.15s ease; }
.tpl:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }
.tpl-head { display: flex; justify-content: space-between; align-items: center; }
.logo { font-weight: 800; color: var(--brand); letter-spacing: 0.02em; }
.version { font-size: 12px; color: var(--muted); }
.tpl h3 { margin: 6px 0 0; font-size: 19px; }
.tpl p { margin: 0; color: var(--ink-2); line-height: 1.55; flex: 1; }
.tpl-foot { display: flex; justify-content: space-between; font-size: 13px; color: var(--muted); margin-top: 10px; }
.go { color: var(--brand); font-weight: 600; }
.list { padding: 6px 0; }
.list-row { display: grid; grid-template-columns: 48px 1.2fr 1.4fr 1fr 1fr; align-items: center; gap: 14px;
  padding: 10px 20px; color: inherit; text-decoration: none; border-top: 1px solid var(--line); }
.list-row:first-child { border-top: 0; }
.list-row:hover { background: #f8fafc; }
.score-badge { width: 40px; height: 40px; border-radius: 12px; color: #fff; display: grid; place-items: center; font-weight: 700;
  background: var(--muted); }
.who { display: flex; flex-direction: column; }
.who small, .what, .when { color: var(--muted); font-size: 14px; }
.status { font-weight: 600; font-size: 14px; }
.when { text-align: right; }
.empty { color: var(--muted); }
@media (max-width: 760px) {
  .list-row { grid-template-columns: 48px 1fr; }
  .what, .status, .when { display: none; }
}
</style>
