import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import RewriteView from '@/views/RewriteView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ForgotPasswordView from '@/views/ForgotPasswordView.vue'
import ProfileView from '@/views/ProfileView.vue'
import RecordsView from '@/views/RecordsView.vue'
import RedeemView from '@/views/RedeemView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/rewrite', name: 'rewrite', component: RewriteView },
    { path: '/records', name: 'records', component: RecordsView },
    { path: '/redeem', name: 'redeem', component: RedeemView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView },
  ],
})

export default router
