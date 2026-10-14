<script setup>
import { computed } from 'vue'

const props = defineProps({
  question: { type: Object, required: true },
  modelValue: { default: undefined },
  error: { type: String, default: null }
})
const emit = defineEmits(['update:modelValue'])

const q = computed(() => props.question)
const scale = computed(() => {
  const out = []
  for (let i = q.value.min; i <= q.value.max; i++) out.push(i)
  return out
})

function set(value) {
  emit('update:modelValue', props.modelValue === value && q.value.type !== 'TEXT' && q.value.type !== 'NUMBER'
    ? undefined
    : value)
}

function onNumber(event) {
  const raw = event.target.value
  emit('update:modelValue', raw === '' ? undefined : Number(raw))
}
</script>

<template>
  <div class="field" :class="{ invalid: error }">
    <label class="label" :for="q.id">
      {{ q.label }}<span v-if="q.required" class="req" aria-label="required">*</span>
    </label>
    <p v-if="q.help" class="help">{{ q.help }}</p>

    <div v-if="q.type === 'YES_NO'" class="seg" role="radiogroup">
      <button type="button" :class="{ on: modelValue === true }" @click="set(true)">Yes</button>
      <button type="button" :class="{ on: modelValue === false }" @click="set(false)">No</button>
    </div>

    <div v-else-if="q.type === 'SCALE'" class="scale">
      <div class="seg" role="radiogroup">
        <button v-for="n in scale" :key="n" type="button" :class="{ on: modelValue === n }" @click="set(n)">{{ n }}</button>
      </div>
      <div v-if="q.minLabel || q.maxLabel" class="ends"><span>{{ q.minLabel }}</span><span>{{ q.maxLabel }}</span></div>
    </div>

    <div v-else-if="q.type === 'CHOICE'" class="choices" role="radiogroup">
      <button v-for="o in q.options" :key="o.value" type="button" class="choice"
              :class="{ on: modelValue === o.value }" @click="set(o.value)">
        <span class="dot"></span>{{ o.label }}
      </button>
    </div>

    <div v-else-if="q.type === 'NUMBER'" class="number">
      <input :id="q.id" type="number" inputmode="decimal" :min="q.min" :max="q.max"
             :value="modelValue ?? ''" @input="onNumber" />
      <span v-if="q.unit" class="unit">{{ q.unit }}</span>
    </div>

    <textarea v-else-if="q.type === 'TEXT'" :id="q.id" rows="3" maxlength="2000"
              :value="modelValue ?? ''" @input="emit('update:modelValue', $event.target.value || undefined)"></textarea>

    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.field { padding: 16px 0; border-top: 1px solid var(--line); }
.field:first-of-type { border-top: 0; }
.label { display: block; font-weight: 600; margin-bottom: 8px; line-height: 1.4; }
.req { color: var(--brand); margin-left: 3px; }
.help { margin: -4px 0 8px; color: var(--muted); font-size: 14px; }
.seg { display: inline-flex; border: 1px solid var(--line-strong); border-radius: 10px; overflow: hidden; }
.seg button { border: 0; border-left: 1px solid var(--line-strong); background: #fff; padding: 8px 16px; min-width: 48px;
  font: inherit; cursor: pointer; color: var(--ink-2); }
.seg button:first-child { border-left: 0; }
.seg button:hover { background: var(--brand-soft); }
.seg button.on { background: var(--brand); color: #fff; }
.scale { display: inline-block; }
.ends { display: flex; justify-content: space-between; gap: 24px; font-size: 12px; color: var(--muted); margin-top: 4px; }
.choices { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 8px; }
.choice { display: flex; align-items: center; gap: 10px; text-align: left; border: 1px solid var(--line-strong);
  border-radius: 10px; background: #fff; padding: 10px 12px; font: inherit; cursor: pointer; color: var(--ink-2); }
.choice:hover { border-color: var(--brand); }
.choice .dot { width: 14px; height: 14px; border-radius: 50%; border: 2px solid var(--line-strong); flex: none; }
.choice.on { border-color: var(--brand); background: var(--brand-soft); color: var(--ink); }
.choice.on .dot { border: 4px solid var(--brand); }
.number { display: flex; align-items: center; gap: 8px; }
.number input { width: 140px; }
.unit { color: var(--muted); }
textarea { width: 100%; resize: vertical; }
.invalid .seg, .invalid input, .invalid textarea, .invalid .choice { border-color: var(--danger); }
.error { color: var(--danger); font-size: 13px; margin: 6px 0 0; }
</style>
