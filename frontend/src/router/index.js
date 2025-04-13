import { createRouter, createWebHistory } from 'vue-router'
import ScoreDisplay from '../components/ScoreDisplay.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: ScoreDisplay
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 