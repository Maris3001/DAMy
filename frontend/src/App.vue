<script setup>
// Khung ứng dụng — header người dùng cho khu công khai; khu admin có layout riêng.
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'

const route = useRoute()
const isAdmin = computed(() => route.meta.admin === true)
</script>

<template>
  <div v-if="!isAdmin" class="flex min-h-screen flex-col">
    <AppHeader />
    <div class="flex-1">
      <router-view />
    </div>
    <AppFooter />
  </div>
  <router-view v-else />
</template>
