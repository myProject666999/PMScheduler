<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import AppLayout from '@/components/AppLayout.vue'

const userStore = useUserStore()
</script>

<template>
  <AppLayout v-if="userStore.isLoggedIn()" />
  <router-view v-else />
</template>
