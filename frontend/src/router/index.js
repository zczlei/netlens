import { createRouter, createWebHistory } from 'vue-router'
import ScoreDisplay from '../components/ScoreDisplay.vue'
import TrafficList from '../components/TrafficList.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/score',
    name: 'Score',
    component: ScoreDisplay
  },
  {
    path: '/traffic-list',
    name: 'TrafficList',
    component: TrafficList
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 