export class ApiError extends Error {
  constructor(status, problem) {
    super(problem?.detail || `Request failed (${status})`)
    this.status = status
    this.problem = problem
  }

  /** Field errors as { questionId|field: message }. */
  get fieldErrors() {
    const out = {}
    for (const e of this.problem?.errors || []) out[e.questionId || e.field] = e.message
    return out
  }
}

async function request(method, url, body, signal) {
  const res = await fetch(url, {
    method,
    signal,
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined
  })
  if (!res.ok) {
    let problem = null
    try { problem = await res.json() } catch { /* not JSON */ }
    throw new ApiError(res.status, problem)
  }
  return res.json()
}

export const api = {
  templates: () => request('GET', '/api/templates'),
  template: (id) => request('GET', `/api/templates/${encodeURIComponent(id)}`),
  evaluate: (id, answers, signal) =>
    request('POST', `/api/templates/${encodeURIComponent(id)}/evaluate`, { answers }, signal),
  submit: (id, payload) => request('POST', `/api/templates/${encodeURIComponent(id)}/submissions`, payload),
  scorecard: (id) => request('GET', `/api/submissions/${encodeURIComponent(id)}`),
  recent: (limit = 8) => request('GET', `/api/submissions?limit=${limit}`),
  pdfUrl: (id) => `/api/submissions/${encodeURIComponent(id)}/pdf`
}
