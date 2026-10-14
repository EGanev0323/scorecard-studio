<template>
  <header class="topbar">
    <RouterLink to="/" class="brand">
      <img src="/favicon.svg" alt="" width="26" height="26" />
      <span>Scorecard Studio</span>
    </RouterLink>
    <nav>
      <a href="/api/docs" target="_blank" rel="noopener">API docs</a>
    </nav>
  </header>
  <main class="page">
    <RouterView />
  </main>
</template>
