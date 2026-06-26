<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <RouterView />
  </div>
</template>

<style scoped>
.app-shell {
  width: 100%;
  min-height: 100vh;
  padding: 2rem 1rem 3rem;
}
</style>
