import { createRouter, createWebHistory } from 'vue-router'
import AdminLoginView from '@/views/AdminLoginView.vue'
import AdminLayout from '@/views/AdminLayout.vue'
import UserManageView from '@/views/UserManageView.vue'
import PresetManageView from '@/views/PresetManageView.vue'
import ConfigManageView from '@/views/ConfigManageView.vue'
import RedeemManageView from '@/views/RedeemManageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: AdminLoginView },
    { path: '/', component: AdminLayout, redirect: '/users', children: [
      { path: 'users', name: 'users', component: UserManageView },
      { path: 'presets', name: 'presets', component: PresetManageView },
      { path: 'redeem', name: 'redeem', component: RedeemManageView },
      { path: 'config', name: 'config', component: ConfigManageView },
    ] },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) return '/login'
  if (to.path === '/login' && token) return '/users'
})

export default router
