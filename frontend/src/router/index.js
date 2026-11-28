import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import OverviewView from '@/views/OverviewView.vue'
import PosView from '@/views/PosView.vue'
import ReportsView from '@/views/ReportsView.vue'
import RecordsView from '@/views/RecordsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { publicShell: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/app',
      component: AdminLayout,
      redirect: '/app/overview',
      children: [
        {
          path: 'overview',
          name: 'app-overview',
          component: OverviewView,
          meta: { group: 'Vận hành', title: 'Tổng quan quán' },
        },
        {
          path: 'pos',
          name: 'app-pos',
          component: PosView,
          meta: { group: 'Vận hành', title: 'Bán hàng tại quầy (POS)' },
        },
        {
          path: 'reports',
          name: 'app-reports',
          component: ReportsView,
          meta: { group: 'Vận hành', title: 'Báo cáo doanh thu' },
        },
        {
          path: 'data',
          name: 'app-data',
          component: RecordsView,
          meta: { group: 'Quản trị', title: 'Dữ liệu hệ thống' },
        },
      ],
    },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
        top: 70,
      }
    }
    return { top: 0 }
  },
})

export default router
