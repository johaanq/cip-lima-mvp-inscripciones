<template>
  <footer class="site-footer">
    <div class="cip-container site-footer__inner">
      <div>
        <p class="site-footer__title">Colegio de Ingenieros del Perú</p>
        <p class="site-footer__subtitle">Consejo Departamental de Lima</p>
        <p class="site-footer__meta">MVP de inscripciones — evento institucional</p>
      </div>

      <div class="site-footer__contact">
        <p><strong>Contacto institucional</strong></p>
        <p>Calle Barcelona 240, San Isidro – Lima</p>
        <p>informes@ciplima.org.pe</p>
        <p>(+01) 202-5000</p>
      </div>
    </div>

    <div class="site-footer__legal">
      <div class="cip-container">
        © {{ new Date().getFullYear() }} Consejo Departamental de Lima CIP. Todos los derechos reservados.
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: auto;
  width: 100%;
  background: var(--cip-gray-900);
  color: #d1d5db;
}

.site-footer__inner {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  padding: 2rem 1.25rem;
}

.site-footer__title {
  color: #fff;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.site-footer__subtitle {
  color: #fca5a5;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.site-footer__meta {
  font-size: 0.88rem;
  color: #9ca3af;
}

.site-footer__contact p {
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.site-footer__contact strong {
  color: #fff;
}

.site-footer__legal {
  border-top: 1px solid #374151;
  padding: 0.85rem 0;
  font-size: 0.82rem;
  color: #9ca3af;
  overflow-wrap: anywhere;
}
</style>
