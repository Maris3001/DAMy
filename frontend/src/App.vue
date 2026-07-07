<script setup>
// Khung ứng dụng — header người dùng cho khu công khai; khu admin có layout riêng.
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
const isAdmin = computed(() => route.meta.admin === true)
</script>

<template>
  <AppHeader v-if="!isAdmin" />
  <router-view />
</template>
