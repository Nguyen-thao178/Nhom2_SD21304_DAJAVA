<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

const navGroups = [
  {
    title: 'VẬN HÀNH & KINH DOANH',
    items: [
      { label: 'Tổng quan', to: '/app/overview', icon: 'dashboard' },
      { label: 'Bán hàng (POS)', to: '/app/pos', icon: 'point_of_sale' },
      { label: 'Báo cáo doanh thu', to: '/app/reports', icon: 'query_stats' },
      { label: 'Đơn hàng & Hóa đơn', to: '/app/data?table=tbl_don_hang', icon: 'receipt_long' },
      { label: 'Bàn & phục vụ', to: '/app/data?table=dining_table', icon: 'table_restaurant' },
      { label: 'Quầy chế biến (KDS)', to: '/app/data?table=invoice_line', icon: 'soup_kitchen' },
    ],
  },
  {
    title: 'HÀNG HÓA & KHO',
    items: [
      { label: 'Quản lý thực đơn', to: '/app/data?table=tbl_san_pham', icon: 'restaurant_menu' },
      { label: 'Ticket pha chế', to: '/app/data?table=ticket', icon: 'local_cafe' },
      { label: 'Kho & nguyên liệu', to: '/app/data?table=tbl_kho_nguyen_lieu', icon: 'inventory_2' },
      { label: 'Mua hàng & NCC', to: '/app/data?table=purchase_order', icon: 'local_shipping' },
    ],
  },
  {
    title: 'QUẢN TRỊ',
    items: [
      { label: 'Khách hàng (CRM)', to: '/app/data?table=tbl_khach_hang', icon: 'group' },
      { label: 'Ca làm & Nhân viên', to: '/app/data?table=tbl_tai_khoan', icon: 'badge' },
      { label: 'Dữ liệu hệ thống', to: '/app/data', icon: 'database', exact: true },
      { label: 'Thiết lập hệ thống', to: '/app/data?table=branch', icon: 'settings' },
    ],
  },
]

const breadcrumbGroup = computed(() => route.meta?.group || 'Vận hành')
const breadcrumbTitle = computed(() => route.meta?.title || 'Tổng quan quán')

function isActive(item) {
  if (item.exact) {
    return route.path === '/app/data' && !route.query.table
  }
  if (item.to.includes('?table=')) {
    const [, queryPart] = item.to.split('?')
    const targetTable = new URLSearchParams(queryPart).get('table')
    return route.path === '/app/data' && route.query.table === targetTable
  }
  return route.path === item.to
}

function handleLogout() {
  router.push('/login')
}
</script>

<template>
  <div class="bg-surface font-body-md text-on-surface antialiased min-h-screen">
    <!-- Mobile Backdrop -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-black/30 z-40 lg:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside
      class="fixed left-0 top-0 h-screen w-[246px] bg-surface-container-lowest border-r border-outline-variant/40 z-50 flex flex-col justify-between overflow-y-auto transition-transform duration-200 lg:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="p-4">
        <RouterLink to="/" class="flex items-center gap-3 px-1 mb-5">
          <div class="w-[39px] h-[39px] bg-primary-container rounded-[11px] flex items-center justify-center shadow-sm">
            <span class="font-headline-md text-on-primary italic font-serif leading-none select-none">P.</span>
          </div>
          <div class="flex flex-col">
            <span class="font-headline-md text-[22px] font-bold tracking-tight text-primary-container leading-none">PolyCafe</span>
            <span class="font-label-sm text-[9px] uppercase tracking-wider text-outline mt-1 font-semibold">Vận hành quán</span>
          </div>
        </RouterLink>

        <div class="rounded-lg border border-outline-variant/50 bg-surface-container-low/40 p-2.5 mb-4">
          <div class="flex items-center gap-1.5">
            <span class="w-1.5 h-1.5 rounded-full bg-primary-container"></span>
            <span class="font-label-sm text-[10px] text-on-surface-variant font-medium tracking-wide">Không gian vận hành</span>
          </div>
          <div class="font-label-md text-on-surface font-semibold truncate mt-0.5">Chi nhánh Hoàn Kiếm #01</div>
        </div>

        <nav class="space-y-4">
          <div v-for="group in navGroups" :key="group.title" class="space-y-1">
            <div class="font-label-sm text-[9px] font-bold tracking-[1.5px] uppercase text-outline px-2.5 py-1">
              {{ group.title }}
            </div>
            <RouterLink
              v-for="item in group.items"
              :key="item.to"
              :to="item.to"
              class="flex items-center gap-3 px-3 py-2 rounded-lg font-label-md transition-colors"
              :class="
                isActive(item)
                  ? 'bg-surface-container-low text-primary-container font-semibold'
                  : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
              "
              @click="sidebarOpen = false"
            >
              <span class="material-symbols-outlined text-[19px]">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </RouterLink>
          </div>
        </nav>
      </div>

      <div class="p-3.5 border-t border-outline-variant/40 bg-surface-container-lowest flex items-center justify-between">
        <div class="flex items-center gap-2.5 overflow-hidden">
          <div class="w-[33px] h-[33px] rounded-full bg-surface-container-high border border-outline-variant flex items-center justify-center font-label-md text-primary-container font-semibold flex-shrink-0">
            NV
          </div>
          <div class="flex flex-col truncate">
            <span class="font-label-md text-on-surface font-semibold truncate leading-tight">Nguyễn Văn Nam</span>
            <span class="font-body-sm text-[11px] text-outline truncate">@barista.nam</span>
          </div>
        </div>
        <button
          class="text-outline hover:text-error transition-colors p-1 rounded-md hover:bg-surface-container-high"
          title="Đăng xuất"
          type="button"
          @click="handleLogout"
        >
          <span class="material-symbols-outlined text-[19px]">logout</span>
        </button>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="lg:pl-[246px] flex flex-col min-h-screen bg-surface">
      <header class="fixed top-0 left-0 lg:left-[246px] right-0 h-[79px] bg-surface/80 backdrop-blur-md border-b border-outline-variant/40 z-40 px-4 sm:px-8 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="lg:hidden p-2 rounded-lg text-on-surface hover:bg-surface-container-high"
            aria-label="Mở menu"
            @click="sidebarOpen = !sidebarOpen"
          >
            <span class="material-symbols-outlined text-[22px]">menu</span>
          </button>
          <div class="flex flex-col">
            <nav class="flex items-center gap-1.5 font-label-md text-outline">
              <span class="hover:text-on-surface transition-colors">{{ breadcrumbGroup }}</span>
              <span class="text-xs">/</span>
              <span class="text-on-surface font-semibold">{{ breadcrumbTitle }}</span>
            </nav>
            <div class="hidden sm:flex font-body-sm text-on-surface-variant items-center gap-2 mt-0.5">
              <span class="inline-block w-1.5 h-1.5 rounded-full bg-secondary"></span>
              <span>Thứ Hai, 24/02/2026 · Ca sáng: 06:30 - 14:30</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <div class="hidden sm:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-surface-container border border-outline-variant/60 font-label-md text-on-surface">
            <span class="w-2 h-2 rounded-full bg-primary-container animate-pulse"></span>
            <span class="font-semibold">Hoàn Kiếm #01</span>
            <span class="material-symbols-outlined text-[16px] text-outline">expand_more</span>
          </div>
          <RouterLink
            to="/"
            title="Về trang chủ khách hàng"
            class="w-8 h-8 rounded-full bg-primary flex items-center justify-center hover:bg-primary-container transition-colors"
          >
            <span class="material-symbols-outlined text-on-primary text-[18px]">home</span>
          </RouterLink>
        </div>
      </header>

      <main class="flex-1 pt-[79px] w-full">
        <RouterView />
      </main>

      <footer class="w-full py-6 text-center border-t border-outline-variant/30 mt-auto bg-surface">
        <p class="font-body-sm text-[11px] text-outline">
          PolyCafe · Chăm chút từng trải nghiệm, từ ly cà phê đến ca vận hành
        </p>
      </footer>
    </div>
  </div>
</template>
