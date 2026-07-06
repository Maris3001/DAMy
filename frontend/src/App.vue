<script setup>
// Khung ứng dụng — header chung + trang theo route hiện tại.
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <AppHeader />
  <router-view />
</template>
