<script setup>
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <header class="site-header">
    <div class="site-header__utility">
      <div class="cip-container site-header__utility-inner">
        <span class="site-header__org-line">
          Colegio de Ingenieros del Perú — Consejo Departamental de Lima
        </span>

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

        <a
          class="site-header__site-link"
          href="https://ciplima.org.pe/"
          target="_blank"
          rel="noopener noreferrer"
        >
          ciplima.org.pe
        </a>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header__utility {
  background: var(--cip-black);
  color: #9ca3af;
  font-size: 0.75rem;
  border-bottom: 1px solid #1f2937;
}

.site-header__utility-inner {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 1rem;
  min-height: 44px;
}

.site-header__org-line {
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.site-header__nav {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.site-header__nav-link {
  color: #e5e7eb;
  font-size: 0.85rem;
  font-weight: 700;
  text-decoration: none;
  padding-bottom: 0.1rem;
  border-bottom: 2px solid transparent;
}

.site-header__nav-link:hover {
  color: #fff;
  text-decoration: none;
}

.site-header__nav-link--active {
  color: #fff;
  border-bottom-color: #fff;
}

.site-header__site-link {
  justify-self: end;
  color: #d1d5db;
  text-decoration: none;
  white-space: nowrap;
}

.site-header__site-link:hover {
  color: #fff;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .site-header__utility-inner {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
    padding: 0.5rem 0;
  }

  .site-header__org-line {
    white-space: normal;
  }

  .site-header__site-link {
    justify-self: center;
  }
}
</style>
