<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api, ApiError } from '../lib/api'
import QuestionField from '../components/QuestionField.vue'
import ScoreRing from '../components/ScoreRing.vue'
import CategoryBars from '../components/CategoryBars.vue'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()

const template = ref(null)
const answers = reactive({})
const respondent = reactive({ name: '', org: '' })
const preview = ref(null)
const submitErrors = ref({})
const previewErrors = ref({})
const fieldErrors = computed(() => ({ ...submitErrors.value, ...previewErrors.value }))
const formError = ref(null)
const loadError = ref(null)
const submitting = ref(false)

let timer = null
let inflight = null

onMounted(async () => {
  try {
    template.value = await api.template(props.id)
    refreshPreview()
  } catch (e) {
    loadError.value = e.message
  }
})
onBeforeUnmount(() => { clearTimeout(timer); inflight?.abort() })

const brandStyle = computed(() => template.value
  ? { '--brand': template.value.branding.primaryColor, '--accent': template.value.branding.accentColor }
  : {})

function cleanAnswers() {
  return Object.fromEntries(Object.entries(answers).filter(([, v]) => v !== undefined && v !== ''))
}

/** Debounced live scoring: only the latest request wins, older ones are aborted. */
function refreshPreview() {
  clearTimeout(timer)
  timer = setTimeout(async () => {
    inflight?.abort()
    inflight = new AbortController()
    try {
      preview.value = await api.evaluate(props.id, cleanAnswers(), inflight.signal)
      previewErrors.value = {}
    } catch (e) {
      if (e.name === 'AbortError') return
      if (e instanceof ApiError && e.status === 422) previewErrors.value = e.fieldErrors
    }
  }, 220)
}

watch(answers, () => {
  // An answered question no longer shows its "required" error from the last submit attempt.
  submitErrors.value = Object.fromEntries(Object.entries(submitErrors.value)
    .filter(([id]) => answers[id] === undefined || answers[id] === ''))
  refreshPreview()
}, { deep: true })
watch(() => respondent.name, (name) => {
  if (name.trim()) {
    const { respondentName, ...rest } = submitErrors.value
    submitErrors.value = rest
  }
})

async function submit() {
  formError.value = null
  if (!respondent.name.trim()) {
    submitErrors.value = { ...submitErrors.value, respondentName: 'Please enter a name' }
    formError.value = 'Some answers need attention.'
    return
  }
  submitting.value = true
  try {
    const saved = await api.submit(props.id, {
      respondentName: respondent.name, respondentOrg: respondent.org, answers: cleanAnswers()
    })
    router.push(`/scorecards/${saved.id}`)
  } catch (e) {
    submitErrors.value = e instanceof ApiError ? e.fieldErrors : {}
    formError.value = Object.keys(submitErrors.value).length ? 'Some answers need attention.' : e.message
    await nextTick()
    document.querySelector('.invalid')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <p v-if="loadError" class="alert">{{ loadError }}</p>

  <div v-if="template" class="layout" :style="brandStyle">
    <form class="form" novalidate @submit.prevent="submit">
      <RouterLink to="/" class="back">← All assessments</RouterLink>
      <div class="intro">
        <span class="logo">{{ template.branding.logoText }}</span>
        <h1>{{ template.name }}</h1>
        <p>{{ template.description }}</p>
      </div>

      <section class="card">
        <h2>About you</h2>
        <div class="two">
          <label :class="{ invalid: fieldErrors.respondentName }">
            <span>Name<span class="req">*</span></span>
            <input v-model="respondent.name" maxlength="120" autocomplete="name" />
            <small v-if="fieldErrors.respondentName" class="error">{{ fieldErrors.respondentName }}</small>
          </label>
          <label>
            <span>Organization</span>
            <input v-model="respondent.org" maxlength="160" autocomplete="organization" />
          </label>
        </div>
      </section>

      <section v-for="(c, i) in template.categories" :key="c.id" class="card">
        <div class="cat-head">
          <span class="step">{{ i + 1 }}</span>
          <div>
            <h2>{{ c.name }}</h2>
            <p>{{ c.description }}</p>
          </div>
        </div>
        <QuestionField v-for="q in c.questions" :key="q.id" :question="q" v-model="answers[q.id]"
                       :error="fieldErrors[q.id]" />
      </section>

      <div class="actions">
        <p v-if="formError" class="error">{{ formError }}</p>
        <button class="btn primary" type="submit" :disabled="submitting">
          {{ submitting ? 'Generating…' : 'Generate scorecard' }}
        </button>
      </div>
    </form>

    <aside class="preview card">
      <div class="preview-head">
        <h2>Live scorecard</h2>
        <span class="completion">{{ preview?.completionPercent ?? 0 }}% complete</span>
      </div>
      <div class="progress"><div :style="{ width: (preview?.completionPercent ?? 0) + '%' }"></div></div>
      <div class="overall">
        <ScoreRing :score="preview?.overallScore ?? null" :color="preview?.status?.color" />
        <div>
          <span v-if="preview?.status" class="pill" :style="{ background: preview.status.color }">
            {{ preview.status.label }}
          </span>
          <span v-else class="pill muted">No answers yet</span>
          <p class="summary">{{ preview?.status?.summary || 'Answer a few questions to see the score.' }}</p>
        </div>
      </div>
      <CategoryBars v-if="preview" :categories="preview.categories" />
    </aside>
  </div>
</template>

<style scoped>
.layout { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 28px; align-items: start; }
.back { color: var(--muted); text-decoration: none; font-size: 14px; }
.intro { margin: 10px 0 20px; }
.intro .logo { font-weight: 800; color: var(--brand); }
.intro h1 { margin: 4px 0 8px; font-size: 30px; letter-spacing: -0.02em; }
.intro p { margin: 0; color: var(--ink-2); line-height: 1.6; }
.form .card { margin-bottom: 18px; }
.card h2 { margin: 0; font-size: 18px; }
.cat-head { display: flex; gap: 14px; align-items: flex-start; margin-bottom: 4px; }
.cat-head p { margin: 4px 0 0; color: var(--muted); font-size: 14px; }
.step { flex: none; width: 30px; height: 30px; border-radius: 9px; background: var(--brand); color: #fff;
  display: grid; place-items: center; font-weight: 700; }
.two { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-top: 12px; }
.two label { display: flex; flex-direction: column; gap: 6px; font-weight: 600; }
.req { color: var(--brand); margin-left: 3px; }
.invalid input { border-color: var(--danger); }
.actions { display: flex; align-items: center; justify-content: flex-end; gap: 16px; margin-bottom: 40px; }
.error { color: var(--danger); font-weight: 400; font-size: 14px; margin: 0; }
.preview { position: sticky; top: 84px; }
.preview-head { display: flex; justify-content: space-between; align-items: baseline; }
.completion { font-size: 13px; color: var(--muted); }
.progress { height: 4px; background: var(--track); border-radius: 99px; margin: 10px 0 16px; overflow: hidden; }
.progress div { height: 100%; background: var(--brand); transition: width 0.3s ease; }
.overall { display: flex; gap: 16px; align-items: center; margin-bottom: 20px; }
.pill { display: inline-block; color: #fff; font-weight: 600; font-size: 13px; padding: 4px 10px; border-radius: 99px; }
.pill.muted { background: var(--track); color: var(--muted); }
.summary { font-size: 14px; color: var(--ink-2); line-height: 1.5; margin: 8px 0 0; }
@media (max-width: 960px) {
  .layout { grid-template-columns: 1fr; }
  .preview { position: static; order: -1; }
  .two { grid-template-columns: 1fr; }
}
</style>
