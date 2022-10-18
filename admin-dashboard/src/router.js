import { createRouter, createWebHistory } from 'vue-router'
import Shortener from './pages/Shortener.vue'
import Database from './pages/Database.vue'
import DatabaseData from './pages/DatabaseData.vue'
import LoadBalancer from './pages/LoadBalancer.vue'
import Login from './pages/Login.vue'
import Logout from './pages/Logout.vue'
import Layout from './partials/Layout.vue'
import { useStore } from '../store'

const routerHistory = createWebHistory()

const router = createRouter({
  history: routerHistory,
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '/',
          name: 'shortener',
          component: Shortener,
        },
        {
          path: '/database',
          name: 'database',
          component: Database,
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: '/database-data/:data',
          name: 'database-data',
          component: DatabaseData,
          props: true,
          meta: {
            requiresAuth: true,
          },
        },
        {
          path: '/load-balancer',
          name: 'load-balancer',
          component: LoadBalancer,
          meta: {
            requiresAuth: true,
          },
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/logout',
      name: 'logout',
      component: Logout,
    },
  ],
})

router.beforeEach((to) => {
  const store = useStore()

  // redirect to login if page requires auth and not logged in
  if (to.meta.requiresAuth && !store.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
