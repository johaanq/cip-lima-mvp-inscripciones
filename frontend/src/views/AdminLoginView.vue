<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError } from '@/services/api'
import { login } from '@/services/authService'
import { isAuthenticated } from '@/services/authSession'

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const enviando = ref(false)
const error = ref('')

onMounted(() => {
  if (isAuthenticated()) {
    router.replace('/admin')
  }
})

async function iniciarSesion() {
  error.value = ''
  enviando.value = true

  try {
    await login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin'
    router.push(redirect)
  } catch (err) {
    error.value = err instanceof ApiError
      ? err.message
      : 'No fue posible iniciar sesión'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <section class="cip-page">
    <div class="cip-container login-layout">
      <header class="page-head">
        <h1 class="cip-page-title">Acceso administrador</h1>
      </header>

      <div class="cip-card login-card">
        <form class="cip-form" @submit.prevent="iniciarSesion">
          <div class="cip-field">
            <label for="username">Usuario</label>
            <input
              id="username"
              v-model="username"
              type="text"
              autocomplete="username"
              :disabled="enviando"
            />
          </div>

          <div class="cip-field">
            <label for="password">Contraseña</label>
            <input
              id="password"
              v-model="password"
              type="password"
              autocomplete="current-password"
              :disabled="enviando"
            />
          </div>

          <p v-if="error" class="cip-form__error">{{ error }}</p>

          <button type="submit" class="cip-btn cip-btn--primary" :disabled="enviando">
            {{ enviando ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.login-layout {
  max-width: 420px;
}

.page-head {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}
</style>
