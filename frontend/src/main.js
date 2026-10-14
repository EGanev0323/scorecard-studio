import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import HomeView from './views/HomeView.vue'
import FillView from './views/FillView.vue'
import ResultView from './views/ResultView.vue'
import './style.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/templates/:id', component: FillView, props: true },
    { path: '/scorecards/:id', component: ResultView, props: true }
  ],
  scrollBehavior: () => ({ top: 0 })
})

createApp(App).use(router).mount('#app')
