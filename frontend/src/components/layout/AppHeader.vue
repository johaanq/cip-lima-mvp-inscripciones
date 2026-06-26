<script setup>
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <header class="site-header">
    <div class="site-header__utility">
      <div class="cip-container site-header__utility-inner">
        <span>Colegio de Ingenieros del Perú — Consejo Departamental de Lima</span>
        <a href="https://ciplima.org.pe/" target="_blank" rel="noopener noreferrer">
          ciplima.org.pe
        </a>
      </div>
    </div>

    <div class="site-header__main">
      <div class="cip-container site-header__main-inner">
        <div class="site-header__brand">
          <p class="site-header__org">Colegio de Ingenieros del Perú</p>
          <p class="site-header__dept">Consejo Departamental de Lima</p>
        </div>

        <nav class="site-header__nav" aria-label="Navegación principal">
          <RouterLink
            to="/"
            class="site-header__nav-link"
            :class="{ 'site-header__nav-link--active': route.path === '/' }"
          >
            Inscripción
          </RouterLink>
          <RouterLink
            to="/admin/login"
            class="site-header__nav-link"
            :class="{ 'site-header__nav-link--active': route.path.startsWith('/admin') }"
          >
            Administración
          </RouterLink>
        </nav>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header__utility {
  background: var(--cip-black);
  color: #9ca3af;
  font-size: 0.75rem;
}

.site-header__utility-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  min-height: 32px;
}

.site-header__utility a {
  color: #d1d5db;
  text-decoration: none;
}

.site-header__utility a:hover {
  color: #fff;
  text-decoration: underline;
}

.site-header__main {
  background: var(--cip-red);
  color: var(--cip-white);
  border-bottom: 1px solid var(--cip-red-dark);
}

.site-header__main-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  padding: 1rem 0;
  flex-wrap: wrap;
}

.site-header__org {
  font-size: 0.72rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  opacity: 0.85;
  margin-bottom: 0.15rem;
}

.site-header__dept {
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.2;
}

.site-header__nav {
  display: flex;
  gap: 1.75rem;
}

.site-header__nav-link {
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.9rem;
  font-weight: 500;
  text-decoration: none;
  padding-bottom: 0.15rem;
  border-bottom: 2px solid transparent;
}

.site-header__nav-link:hover {
  color: #fff;
  text-decoration: none;
}

.site-header__nav-link--active {
  color: #fff;
  font-weight: 600;
  border-bottom-color: #fff;
}
</style>
