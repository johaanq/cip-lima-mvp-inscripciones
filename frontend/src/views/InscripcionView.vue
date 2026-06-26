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
const imagenInputRef = ref(null)
const imagenPreviewUrl = ref('')
const arrastrandoArchivo = ref(false)
const errorImagen = ref('')

const TAMANO_MAXIMO_IMAGEN = 5 * 1024 * 1024
const TIPOS_IMAGEN_PERMITIDOS = ['image/jpeg', 'image/png', 'image/jpg']

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
  revocarPreviewImagen()
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
  if (errorImagen.value) {
    return errorImagen.value
  }
  return null
}

function formatearTamano(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function revocarPreviewImagen() {
  if (imagenPreviewUrl.value) {
    URL.revokeObjectURL(imagenPreviewUrl.value)
    imagenPreviewUrl.value = ''
  }
}

function procesarArchivo(archivo) {
  errorImagen.value = ''

  if (!archivo) {
    quitarImagen()
    return
  }

  if (!TIPOS_IMAGEN_PERMITIDOS.includes(archivo.type)) {
    errorImagen.value = 'Solo se permiten imágenes JPG o PNG'
    if (imagenInputRef.value) imagenInputRef.value.value = ''
    return
  }

  if (archivo.size > TAMANO_MAXIMO_IMAGEN) {
    errorImagen.value = 'La imagen no debe superar los 5 MB'
    if (imagenInputRef.value) imagenInputRef.value.value = ''
    return
  }

  revocarPreviewImagen()
  imagen.value = archivo
  imagenPreviewUrl.value = URL.createObjectURL(archivo)
}

function quitarImagen() {
  revocarPreviewImagen()
  imagen.value = null
  errorImagen.value = ''
  if (imagenInputRef.value) {
    imagenInputRef.value.value = ''
  }
}

function abrirSelectorArchivo() {
  if (puedeEnviar.value) {
    imagenInputRef.value?.click()
  }
}

function onDragOver(event) {
  event.preventDefault()
  if (puedeEnviar.value) {
    arrastrandoArchivo.value = true
  }
}

function onDragLeave() {
  arrastrandoArchivo.value = false
}

function onDrop(event) {
  event.preventDefault()
  arrastrandoArchivo.value = false
  if (!puedeEnviar.value) return

  const [archivo] = event.dataTransfer?.files ?? []
  procesarArchivo(archivo)
}

function onArchivoSeleccionado(event) {
  const [archivo] = event.target.files
  procesarArchivo(archivo ?? null)
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
  quitarImagen()
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

              <div class="cip-field cip-field--file">
                <span id="imagen-label" class="cip-field__label">Imagen del DNI del menor</span>

                <div
                  v-if="!imagen"
                  class="file-drop"
                  :class="{
                    'file-drop--active': arrastrandoArchivo,
                    'file-drop--disabled': !puedeEnviar,
                  }"
                  role="button"
                  tabindex="0"
                  aria-labelledby="imagen-label"
                  @click="abrirSelectorArchivo"
                  @keydown.enter.prevent="abrirSelectorArchivo"
                  @keydown.space.prevent="abrirSelectorArchivo"
                  @dragover="onDragOver"
                  @dragleave="onDragLeave"
                  @drop="onDrop"
                >
                  <svg
                    class="file-drop__icon"
                    xmlns="http://www.w3.org/2000/svg"
                    width="32"
                    height="32"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    aria-hidden="true"
                  >
                    <path d="M12 16V4m0 0 4 4m-4-4-4 4" stroke-linecap="round" stroke-linejoin="round" />
                    <path
                      d="M4 14v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                  <p class="file-drop__title">Arrastra la imagen aquí o haz clic para seleccionar</p>
                  <p class="file-drop__hint">JPG o PNG · máximo 5 MB</p>
                </div>

                <div v-else class="file-preview">
                  <img
                    :src="imagenPreviewUrl"
                    alt="Vista previa del DNI del menor"
                    class="file-preview__img"
                  />
                  <div class="file-preview__body">
                    <p class="file-preview__name">{{ imagen.name }}</p>
                    <p class="file-preview__meta">{{ formatearTamano(imagen.size) }}</p>
                    <div class="file-preview__actions">
                      <button
                        type="button"
                        class="cip-btn cip-btn--secondary cip-btn--small"
                        :disabled="!puedeEnviar"
                        @click="abrirSelectorArchivo"
                      >
                        Cambiar
                      </button>
                      <button
                        type="button"
                        class="cip-btn cip-btn--secondary cip-btn--small"
                        :disabled="!puedeEnviar"
                        @click="quitarImagen"
                      >
                        Quitar
                      </button>
                    </div>
                  </div>
                </div>

                <input
                  ref="imagenInputRef"
                  id="imagen"
                  type="file"
                  class="file-input-hidden"
                  accept="image/jpeg,image/png,image/jpg"
                  :disabled="!puedeEnviar"
                  @change="onArchivoSeleccionado"
                />

                <p v-if="errorImagen" class="file-field__error">{{ errorImagen }}</p>
                <p v-else-if="!imagen" class="cip-field__help">
                  Fotografía legible del documento de identidad del menor.
                </p>
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
  width: 100%;
  min-width: 0;
}

.page-head {
  margin-bottom: 1.75rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.inscripcion-grid {
  display: grid;
  grid-template-columns: minmax(0, 240px) minmax(0, 1fr);
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

.cip-field--file {
  position: relative;
}

.cip-field__label {
  display: block;
  font-weight: 600;
  font-size: 0.88rem;
  color: var(--color-heading);
  margin-bottom: 0.45rem;
}

.file-input-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.file-drop {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  min-height: 140px;
  padding: 1.25rem 1rem;
  border: 1px dashed var(--color-border);
  background: var(--cip-white);
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
  text-align: center;
}

.file-drop:hover:not(.file-drop--disabled) {
  border-color: var(--cip-red);
  background: var(--cip-red-soft);
}

.file-drop:focus-visible {
  outline: 2px solid var(--cip-red);
  outline-offset: 2px;
}

.file-drop--active {
  border-color: var(--cip-red);
  background: var(--cip-red-soft);
}

.file-drop--disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.file-drop__icon {
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.file-drop--active .file-drop__icon,
.file-drop:hover:not(.file-drop--disabled) .file-drop__icon {
  color: var(--cip-red);
}

.file-drop__title {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--color-heading);
}

.file-drop__hint {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.file-preview {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  padding: 0.85rem;
  border: 1px solid var(--color-border);
  background: var(--cip-white);
}

.file-preview__img {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  object-fit: cover;
  border: 1px solid var(--color-border);
  background: var(--cip-gray-100);
}

.file-preview__body {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 0.2rem;
}

.file-preview__name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-heading);
  word-break: break-all;
}

.file-preview__meta {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 0.35rem;
}

.file-preview__actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.file-field__error {
  margin-top: 0.45rem;
  font-size: 0.85rem;
  color: var(--cip-red);
  font-weight: 500;
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

  .file-preview {
    flex-direction: column;
    align-items: stretch;
  }

  .file-preview__img {
    width: 100%;
    height: auto;
    max-height: 160px;
  }
}
</style>
