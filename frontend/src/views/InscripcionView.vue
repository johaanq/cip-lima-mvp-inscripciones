<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ApiError } from '@/services/api'
import { obtenerEstadoEvento, registrarInscripcion } from '@/services/inscripcionService'

const estadoEvento = ref(null)
const cargandoEvento = ref(true)
const errorEvento = ref('')

const dniColegiado = ref('')
const nombreColegiado = ref('')
const dniMenor = ref('')
const imagen = ref(null)

const enviando = ref(false)
const errorFormulario = ref('')
const resultado = ref(null)
const resultadoModalRef = ref(null)

const inscripcionesAbiertas = computed(() => estadoEvento.value?.inscripcionesAbiertas ?? false)

const puedeEnviar = computed(() =>
  inscripcionesAbiertas.value &&
  !enviando.value &&
  !cargandoEvento.value
)

const resultadoEsRechazo = computed(() => resultado.value?.estado === 'RECHAZADO')

function onEscapeKey(event) {
  if (event.key === 'Escape' && resultado.value) {
    cerrarResultadoModal()
  }
}

watch(resultado, (valor) => {
  document.body.style.overflow = valor ? 'hidden' : ''
  if (valor) {
    document.addEventListener('keydown', onEscapeKey)
    requestAnimationFrame(() => resultadoModalRef.value?.focus())
  } else {
    document.removeEventListener('keydown', onEscapeKey)
  }
})

onMounted(async () => {
  await cargarEstadoEvento()
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onEscapeKey)
  document.body.style.overflow = ''
})

async function cargarEstadoEvento() {
  cargandoEvento.value = true
  errorEvento.value = ''

  try {
    estadoEvento.value = await obtenerEstadoEvento()
  } catch (error) {
    errorEvento.value = error instanceof ApiError
      ? error.message
      : 'No fue posible consultar el estado del evento'
  } finally {
    cargandoEvento.value = false
  }
}

function validarFormulario() {
  if (!/^\d{8}$/.test(dniColegiado.value.trim())) {
    return 'El DNI del colegiado debe tener 8 dígitos'
  }
  if (!nombreColegiado.value.trim()) {
    return 'El nombre del colegiado es obligatorio'
  }
  if (!/^\d{8}$/.test(dniMenor.value.trim())) {
    return 'El DNI del menor debe tener 8 dígitos'
  }
  if (!imagen.value) {
    return 'Debe adjuntar la imagen del DNI del menor'
  }
  return null
}

function onArchivoSeleccionado(event) {
  const [archivo] = event.target.files
  imagen.value = archivo ?? null
}

async function enviarInscripcion() {
  errorFormulario.value = ''
  resultado.value = null

  const errorValidacion = validarFormulario()
  if (errorValidacion) {
    errorFormulario.value = errorValidacion
    return
  }

  enviando.value = true

  try {
    resultado.value = await registrarInscripcion({
      dniColegiado: dniColegiado.value.trim(),
      nombreColegiado: nombreColegiado.value.trim(),
      dniMenor: dniMenor.value.trim(),
      imagen: imagen.value,
    })
  } catch (error) {
    errorFormulario.value = error instanceof ApiError
      ? error.message
      : 'No fue posible registrar la inscripción'
  } finally {
    enviando.value = false
  }
}

function reiniciarFormulario() {
  dniColegiado.value = ''
  nombreColegiado.value = ''
  dniMenor.value = ''
  imagen.value = null
  errorFormulario.value = ''
  resultado.value = null
}

function cerrarResultadoModal() {
  reiniciarFormulario()
}
</script>

<template>
  <section class="cip-page">
    <div class="cip-container inscripcion-layout">
      <header class="page-head">
        <h1 class="cip-page-title">Inscripción — Día del Padre</h1>
      </header>

      <div v-if="cargandoEvento" class="cip-panel cip-panel--info">
        Consultando disponibilidad del evento...
      </div>

      <div v-else-if="errorEvento" class="cip-panel cip-panel--error">
        {{ errorEvento }}
        <button type="button" class="cip-btn cip-btn--secondary" @click="cargarEstadoEvento">
          Reintentar
        </button>
      </div>

      <div v-else-if="estadoEvento" class="inscripcion-grid">
        <aside class="evento-aside">
          <h2 class="cip-section__title">Datos del evento</h2>
          <dl class="evento-aside__list">
            <div class="evento-aside__row">
              <dt>Sede</dt>
              <dd>{{ estadoEvento.sedeConsejo }}</dd>
            </div>
            <div class="evento-aside__row">
              <dt>Cupo disponible</dt>
              <dd>{{ estadoEvento.cupoDisponible }} de {{ estadoEvento.cupoMaximo }}</dd>
            </div>
            <div class="evento-aside__row">
              <dt>Estado</dt>
              <dd>{{ inscripcionesAbiertas ? 'Inscripciones abiertas' : 'Aforo completo' }}</dd>
            </div>
          </dl>
        </aside>

        <div class="inscripcion-main">
          <div v-if="!inscripcionesAbiertas" class="cip-panel cip-panel--warning">
            El evento ha alcanzado su aforo máximo. No se aceptan nuevas inscripciones.
          </div>

          <section v-else class="cip-section">
            <h2 class="cip-section__title">Formulario de inscripción</h2>

            <form class="cip-form" @submit.prevent="enviarInscripcion">
              <div class="form-row">
                <div class="cip-field">
                  <label for="dniColegiado">DNI del colegiado</label>
                  <input
                    id="dniColegiado"
                    v-model="dniColegiado"
                    type="text"
                    inputmode="numeric"
                    maxlength="8"
                    autocomplete="off"
                    placeholder="12345678"
                    :disabled="!puedeEnviar"
                  />
                </div>

                <div class="cip-field">
                  <label for="dniMenor">DNI del menor</label>
                  <input
                    id="dniMenor"
                    v-model="dniMenor"
                    type="text"
                    inputmode="numeric"
                    maxlength="8"
                    autocomplete="off"
                    placeholder="11223344"
                    :disabled="!puedeEnviar"
                  />
                </div>
              </div>

              <div class="cip-field">
                <label for="nombreColegiado">Nombre del colegiado</label>
                <input
                  id="nombreColegiado"
                  v-model="nombreColegiado"
                  type="text"
                  maxlength="200"
                  autocomplete="name"
                  placeholder="Nombre completo"
                  :disabled="!puedeEnviar"
                />
              </div>

              <div class="cip-field">
                <label for="imagen">Imagen del DNI del menor</label>
                <input
                  id="imagen"
                  type="file"
                  accept="image/jpeg,image/png,image/jpg"
                  :disabled="!puedeEnviar"
                  @change="onArchivoSeleccionado"
                />
                <p class="cip-field__help">JPG o PNG, máximo 5 MB.</p>
              </div>

              <p v-if="errorFormulario" class="cip-form__error">{{ errorFormulario }}</p>

              <div class="form-actions">
                <button type="submit" class="cip-btn cip-btn--primary" :disabled="!puedeEnviar">
                  {{ enviando ? 'Enviando...' : 'Registrar inscripción' }}
                </button>
              </div>
            </form>
          </section>
        </div>
      </div>

      <Teleport to="body">
        <div
          v-if="resultado"
          ref="resultadoModalRef"
          class="resultado-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="resultado-modal-title"
          tabindex="-1"
        >
          <button
            type="button"
            class="resultado-modal__backdrop"
            aria-label="Cerrar resultado"
            @click="cerrarResultadoModal"
          />

          <div
            class="resultado-modal__panel"
            :class="{ 'resultado-modal__panel--rechazado': resultadoEsRechazo }"
          >
            <div class="resultado-modal__head">
              <h2 id="resultado-modal-title" class="resultado-modal__title">Resultado</h2>
              <span class="resultado-modal__estado">{{ resultado.estado }}</span>
            </div>

            <dl class="resultado-modal__meta">
              <div class="resultado-modal__row">
                <dt>ID</dt>
                <dd>{{ resultado.id }}</dd>
              </div>
              <div class="resultado-modal__row">
                <dt>Mensaje</dt>
                <dd>{{ resultado.mensaje }}</dd>
              </div>
              <div v-if="resultado.motivoRechazo" class="resultado-modal__row">
                <dt>Motivo</dt>
                <dd>{{ resultado.motivoRechazo }}</dd>
              </div>
            </dl>

            <button type="button" class="cip-btn cip-btn--primary" @click="cerrarResultadoModal">
              Nueva inscripción
            </button>
          </div>
        </div>
      </Teleport>
    </div>
  </section>
</template>

<style scoped>
.inscripcion-layout {
  max-width: 960px;
}

.page-head {
  margin-bottom: 1.75rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.inscripcion-grid {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 2rem;
  align-items: start;
}

.evento-aside {
  padding-top: 0.15rem;
}

.evento-aside .cip-section__title {
  margin-bottom: 0.85rem;
}

.evento-aside__list {
  display: grid;
  gap: 0.85rem;
}

.evento-aside__row dt {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 0.15rem;
}

.evento-aside__row dd {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading);
}

.inscripcion-main {
  display: grid;
  gap: 2rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-actions {
  padding-top: 0.25rem;
}

.resultado-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.resultado-modal__backdrop {
  position: absolute;
  inset: 0;
  border: none;
  background: rgba(17, 17, 17, 0.72);
  cursor: pointer;
}

.resultado-modal__panel {
  position: relative;
  z-index: 1;
  width: min(480px, 100%);
  background: var(--cip-white);
  border: 1px solid var(--color-border);
  padding: 1.5rem;
}

.resultado-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
  padding-bottom: 0.85rem;
  border-bottom: 1px solid var(--color-border);
}

.resultado-modal__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-heading);
}

.resultado-modal__estado {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-heading);
}

.resultado-modal__panel--rechazado .resultado-modal__estado {
  color: var(--cip-red);
}

.resultado-modal__meta {
  display: grid;
  gap: 0.85rem;
  margin-bottom: 1.5rem;
}

.resultado-modal__row dt {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 0.15rem;
}

.resultado-modal__row dd {
  font-size: 0.95rem;
  color: var(--color-heading);
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 720px) {
  .inscripcion-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
