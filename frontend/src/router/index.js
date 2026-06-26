import { createRouter, createWebHistory } from 'vue-router'
import InscripcionView from '../views/InscripcionView.vue'
import AdminLoginView from '../views/AdminLoginView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import { isAuthenticated } from '../services/authSession'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'inscripcion',
      component: InscripcionView,
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLoginView,
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminDashboardView,
      beforeEnter: (to, from, next) => {
        if (!isAuthenticated()) {
          next({ name: 'admin-login', query: { redirect: to.fullPath } })
          return
        }
        next()
      },
    },
  ],
})

export default router
