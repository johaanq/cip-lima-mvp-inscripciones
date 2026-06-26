import { createRouter, createWebHistory } from 'vue-router'
import InscripcionView from '../views/InscripcionView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'inscripcion',
      component: InscripcionView,
    },
  ],
})

export default router
