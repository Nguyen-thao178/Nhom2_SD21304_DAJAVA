<script setup>
import { ref } from 'vue'

const mobileMenuOpen = ref(false)

const navLinks = [
  { label: 'Trang chủ', href: '#home', path: 'trang-chu' },
  { label: 'Thực đơn', href: '#menu', path: 'thuc-don' },
  { label: 'Câu chuyện', href: '#story', path: 'cau-chuyen' },
  { label: 'Không gian', href: '#space', path: 'cua-hang' },
  { label: 'Liên hệ', href: '#contact', path: 'lien-he' },
]

function closeMenu() {
  mobileMenuOpen.value = false
}
</script>

<template>
  <header class="fixed top-0 left-0 w-full z-50 bg-surface-container-lowest/90 backdrop-blur-xl shadow-[0_1px_8px_rgba(0,0,0,0.04)]">
    <div class="h-[70px] max-w-7xl mx-auto px-margin flex items-center justify-between gap-gutter">
      <a href="#home" class="flex items-center gap-space-sm">
        <div class="w-9 h-9 rounded-lg bg-primary flex items-center justify-center">
          <span class="font-headline-md text-headline-md text-on-primary">P.</span>
        </div>
        <span class="font-headline-sm text-headline-sm text-primary tracking-tight">PolyCafe</span>
      </a>

      <nav class="hidden md:flex items-center gap-space-lg">
        <a
          v-for="link in navLinks"
          :key="link.path"
          :href="link.href"
          :data-path="link.path"
          class="font-body-md text-body-md text-on-surface-variant hover:text-primary transition-colors"
        >
          {{ link.label }}
        </a>
      </nav>

      <div class="flex items-center gap-space-md">
        <a
          class="inline-flex items-center justify-center px-space-md py-space-sm rounded-lg bg-primary text-on-primary font-label-lg text-label-lg hover:bg-primary-container transition-colors shadow-sm"
          data-path="dat-ban"
          href="#contact"
        >
          Đặt bàn
        </a>
        <RouterLink
          to="/login"
          title="Đăng nhập nhân viên"
          class="w-8 h-8 rounded-full bg-primary flex items-center justify-center hover:bg-primary-container transition-colors"
        >
          <span class="material-symbols-outlined text-on-primary text-[18px]">person</span>
        </RouterLink>
        <button
          type="button"
          class="md:hidden w-9 h-9 rounded-lg flex items-center justify-center text-primary hover:bg-surface-container-low transition-colors"
          aria-label="Mở menu"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <span class="material-symbols-outlined text-[22px]">
            {{ mobileMenuOpen ? 'close' : 'menu' }}
          </span>
        </button>
      </div>
    </div>

    <div
      v-if="mobileMenuOpen"
      class="md:hidden bg-surface-container-lowest border-t border-[#e6e8df] px-margin py-space-md flex flex-col gap-space-sm"
    >
      <a
        v-for="link in navLinks"
        :key="link.path"
        :href="link.href"
        class="py-2 font-body-md text-on-surface-variant hover:text-primary transition-colors"
        @click="closeMenu"
      >
        {{ link.label }}
      </a>
    </div>
  </header>
</template>
