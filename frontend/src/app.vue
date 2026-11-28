<script setup>
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()
</script>

<template>
  <div v-if="route.meta.publicShell" class="min-h-screen flex flex-col bg-surface text-on-surface">
    <AppHeader />
    <main class="w-full pt-[70px] bg-surface flex-1">
      <RouterView />
    </main>
    <AppFooter />
  </div>
  <RouterView v-else />
</template>
