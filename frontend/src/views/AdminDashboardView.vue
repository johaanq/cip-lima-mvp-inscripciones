<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/services/api'
import {
  aprobarSolicitud,
  listarAprobadas,
  listarPendientes,
  listarRechazadas,
  obtenerImagenSolicitud,
  obtenerMetricas,
  rechazarSolicitud,
} from '@/services/adminService'
import { logout } from '@/services/authService'

const router = useRouter()

const metricas = ref(null)
const pendientes = ref([])
const aprobadas = ref([])
const rechazadas = ref([])
const pestañaActiva = ref('pendientes')
const cargando = ref(true)
const error = ref('')
const mensaje = ref('')

const imagenUrl = ref('')
const imagenSolicitudId = ref(null)
const cargandoImagen = ref(false)

const rechazoActivoId = ref(null)
const observacionRechazo = ref('')
const procesandoId = ref(null)
const imagenModalRef = ref(null)

function onEscapeKey(event) {
  if (event.key === 'Escape') {
    cerrarImagenModal()
  }
}

watch(
  () => cargandoImagen.value || Boolean(imagenUrl.value),
  (abierto) => {
    document.body.style.overflow = abierto ? 'hidden' : ''
    if (abierto) {
      document.addEventListener('keydown', onEscapeKey)
      requestAnimationFrame(() => imagenModalRef.value?.focus())
    } else {
      document.removeEventListener('keydown', onEscapeKey)
    }
  }
)

onMounted(async () => {
  await cargarPanel()
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onEscapeKey)
  revocarImagen()
  document.body.style.overflow = ''
})

function cerrarImagenModal() {
  revocarImagen()
}

async function cargarPanel() {
  cargando.value = true
  error.value = ''

  try {
    const [metricasData, pendientesData, aprobadasData, rechazadasData] = await Promise.all([
      obtenerMetricas(),
      listarPendientes(),
      listarAprobadas(),
      listarRechazadas(),
    ])
    metricas.value = metricasData
    pendientes.value = pendientesData
    aprobadas.value = aprobadasData
    rechazadas.value = rechazadasData
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      logout()
      router.replace({ name: 'admin-login', query: { redirect: '/admin' } })
      return
    }
    error.value = err instanceof ApiError
      ? err.message
      : 'No fue posible cargar el panel administrador'
  } finally {
    cargando.value = false
  }
}

function cerrarSesion() {
  logout()
  router.push('/admin/login')
}

function formatearFecha(fecha) {
  return new Date(fecha).toLocaleString('es-PE')
}

function etiquetaOrigen(origen) {
  if (origen === 'ADMIN') return 'Administrador'
  if (origen === 'AUTOMATICO') return 'Automático'
  return origen ?? '—'
}

const pestañas = [
  { id: 'pendientes', label: 'Pendientes', contador: () => pendientes.value.length },
  { id: 'aprobadas', label: 'Aprobadas', contador: () => aprobadas.value.length },
  { id: 'rechazadas', label: 'Rechazadas', contador: () => rechazadas.value.length },
]

function revocarImagen() {
  if (imagenUrl.value) {
    URL.revokeObjectURL(imagenUrl.value)
    imagenUrl.value = ''
    imagenSolicitudId.value = null
  }
}

async function verImagen(id) {
  revocarImagen()
  cargandoImagen.value = true
  mensaje.value = ''

  try {
    imagenUrl.value = await obtenerImagenSolicitud(id)
    imagenSolicitudId.value = id
  } catch (err) {
    mensaje.value = err instanceof ApiError
      ? err.message
      : 'No fue posible cargar la imagen'
  } finally {
    cargandoImagen.value = false
  }
}

function iniciarRechazo(id) {
  rechazoActivoId.value = id
  observacionRechazo.value = ''
  mensaje.value = ''
}

function cancelarRechazo() {
  rechazoActivoId.value = null
  observacionRechazo.value = ''
}

async function confirmarAprobacion(id) {
  procesandoId.value = id
  mensaje.value = ''
  error.value = ''

  try {
    const resultado = await aprobarSolicitud(id)
    mensaje.value = resultado.mensaje
    if (imagenSolicitudId.value === id) {
      revocarImagen()
    }
    await cargarPanel()
  } catch (err) {
    error.value = err instanceof ApiError
      ? err.message
      : 'No fue posible aprobar la solicitud'
  } finally {
    procesandoId.value = null
  }
}

async function confirmarRechazo(id) {
  if (!observacionRechazo.value.trim()) {
    error.value = 'La observación de rechazo es obligatoria'
    return
  }

  procesandoId.value = id
  mensaje.value = ''
  error.value = ''

  try {
    const resultado = await rechazarSolicitud(id, observacionRechazo.value.trim())
    mensaje.value = resultado.mensaje
    cancelarRechazo()
    if (imagenSolicitudId.value === id) {
      revocarImagen()
    }
    await cargarPanel()
  } catch (err) {
    error.value = err instanceof ApiError
      ? err.message
      : 'No fue posible rechazar la solicitud'
  } finally {
    procesandoId.value = null
  }
}
</script>

<template>
  <section class="cip-page">
    <div class="cip-container">
      <header class="admin-header">
        <h1 class="cip-page-title">Panel administrador</h1>
        <button type="button" class="cip-btn cip-btn--secondary" @click="cerrarSesion">
          Cerrar sesión
        </button>
      </header>

      <div v-if="cargando" class="cip-panel cip-panel--info">Cargando panel...</div>

      <div v-else-if="error" class="cip-panel cip-panel--error">
        {{ error }}
        <button type="button" class="cip-btn cip-btn--secondary" @click="cargarPanel">
          Reintentar
        </button>
      </div>

      <template v-else>
        <p v-if="mensaje" class="cip-panel cip-panel--success">{{ mensaje }}</p>

        <div v-if="metricas" class="cip-stats">
          <article class="cip-stat">
            <span>Total</span>
            <strong>{{ metricas.total }}</strong>
          </article>
          <article class="cip-stat">
            <span>Aprobados</span>
            <strong>{{ metricas.aprobados }}</strong>
          </article>
          <article class="cip-stat">
            <span>Rechazados</span>
            <strong>{{ metricas.rechazados }}</strong>
          </article>
          <article class="cip-stat">
            <span>Pendientes</span>
            <strong>{{ metricas.pendientes }}</strong>
          </article>
          <article class="cip-stat cip-stat--highlight">
            <span>Cupo disponible</span>
            <strong>{{ metricas.cupoDisponible }}</strong>
          </article>
        </div>

        <section class="cip-section">
          <div class="solicitudes-tabs" role="tablist" aria-label="Historial de solicitudes">
            <button
              v-for="pestaña in pestañas"
              :key="pestaña.id"
              type="button"
              role="tab"
              class="solicitudes-tabs__tab"
              :class="{ 'solicitudes-tabs__tab--active': pestañaActiva === pestaña.id }"
              :aria-selected="pestañaActiva === pestaña.id"
              @click="pestañaActiva = pestaña.id"
            >
              {{ pestaña.label }}
              <span class="solicitudes-tabs__count">{{ pestaña.contador() }}</span>
            </button>
          </div>

          <div
            v-show="pestañaActiva === 'pendientes'"
            role="tabpanel"
            aria-label="Solicitudes pendientes"
          >
            <p v-if="pendientes.length === 0" class="cip-panel cip-panel--info">
              No hay solicitudes pendientes de revisión.
            </p>

            <div v-else class="cip-table-wrap">
              <table class="cip-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>DNI colegiado</th>
                    <th>Nombre</th>
                    <th>DNI menor</th>
                    <th>Fecha</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="solicitud in pendientes" :key="solicitud.id">
                    <td>{{ solicitud.id }}</td>
                    <td>{{ solicitud.dniColegiado }}</td>
                    <td>{{ solicitud.nombreColegiado }}</td>
                    <td>{{ solicitud.dniMenor }}</td>
                    <td>{{ formatearFecha(solicitud.createdAt) }}</td>
                    <td>
                      <div class="cip-actions">
                        <button
                          type="button"
                          class="cip-btn cip-btn--secondary cip-btn--small"
                          :disabled="procesandoId === solicitud.id"
                          @click="verImagen(solicitud.id)"
                        >
                          Ver imagen
                        </button>
                        <button
                          type="button"
                          class="cip-btn cip-btn--primary cip-btn--small"
                          :disabled="procesandoId === solicitud.id"
                          @click="confirmarAprobacion(solicitud.id)"
                        >
                          Aprobar
                        </button>
                        <button
                          type="button"
                          class="cip-btn cip-btn--danger cip-btn--small"
                          :disabled="procesandoId === solicitud.id"
                          @click="iniciarRechazo(solicitud.id)"
                        >
                          Rechazar
                        </button>
                      </div>

                      <div v-if="rechazoActivoId === solicitud.id" class="rechazo">
                        <label :for="`observacion-${solicitud.id}`">Observación</label>
                        <textarea
                          :id="`observacion-${solicitud.id}`"
                          v-model="observacionRechazo"
                          rows="3"
                          placeholder="Indique el motivo del rechazo"
                        />
                        <div class="rechazo__acciones">
                          <button
                            type="button"
                            class="cip-btn cip-btn--danger cip-btn--small"
                            :disabled="procesandoId === solicitud.id"
                            @click="confirmarRechazo(solicitud.id)"
                          >
                            Confirmar rechazo
                          </button>
                          <button
                            type="button"
                            class="cip-btn cip-btn--secondary cip-btn--small"
                            @click="cancelarRechazo"
                          >
                            Cancelar
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div
            v-show="pestañaActiva === 'aprobadas'"
            role="tabpanel"
            aria-label="Solicitudes aprobadas"
          >
            <p v-if="aprobadas.length === 0" class="cip-panel cip-panel--info">
              Aún no hay solicitudes aprobadas.
            </p>

            <div v-else class="cip-table-wrap">
              <table class="cip-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>DNI colegiado</th>
                    <th>Nombre</th>
                    <th>DNI menor</th>
                    <th>Solicitud</th>
                    <th>Aprobación</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="solicitud in aprobadas" :key="solicitud.id">
                    <td>{{ solicitud.id }}</td>
                    <td>{{ solicitud.dniColegiado }}</td>
                    <td>{{ solicitud.nombreColegiado }}</td>
                    <td>{{ solicitud.dniMenor }}</td>
                    <td>{{ formatearFecha(solicitud.createdAt) }}</td>
                    <td>{{ formatearFecha(solicitud.updatedAt) }}</td>
                    <td>
                      <button
                        type="button"
                        class="cip-btn cip-btn--secondary cip-btn--small"
                        @click="verImagen(solicitud.id)"
                      >
                        Ver imagen
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div
            v-show="pestañaActiva === 'rechazadas'"
            role="tabpanel"
            aria-label="Solicitudes rechazadas"
          >
            <p v-if="rechazadas.length === 0" class="cip-panel cip-panel--info">
              Aún no hay solicitudes rechazadas.
            </p>

            <div v-else class="cip-table-wrap">
              <table class="cip-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>DNI colegiado</th>
                    <th>Nombre</th>
                    <th>DNI menor</th>
                    <th>Solicitud</th>
                    <th>Rechazo</th>
                    <th>Origen</th>
                    <th>Motivo</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="solicitud in rechazadas" :key="solicitud.id">
                    <td>{{ solicitud.id }}</td>
                    <td>{{ solicitud.dniColegiado }}</td>
                    <td>{{ solicitud.nombreColegiado }}</td>
                    <td>{{ solicitud.dniMenor }}</td>
                    <td>{{ formatearFecha(solicitud.createdAt) }}</td>
                    <td>{{ formatearFecha(solicitud.updatedAt) }}</td>
                    <td>{{ etiquetaOrigen(solicitud.origenRechazo) }}</td>
                    <td class="motivo-cell">{{ solicitud.motivoRechazo || '—' }}</td>
                    <td>
                      <button
                        type="button"
                        class="cip-btn cip-btn--secondary cip-btn--small"
                        @click="verImagen(solicitud.id)"
                      >
                        Ver imagen
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </template>

      <Teleport to="body">
        <div
          v-if="cargandoImagen || imagenUrl"
          ref="imagenModalRef"
          class="imagen-modal"
          role="dialog"
          aria-modal="true"
          aria-label="Imagen del DNI del menor"
          tabindex="-1"
        >
          <button
            type="button"
            class="imagen-modal__backdrop"
            aria-label="Cerrar imagen"
            @click="cerrarImagenModal"
          />

          <div class="imagen-modal__content">
            <button
              type="button"
              class="imagen-modal__close cip-btn cip-btn--secondary cip-btn--small"
              @click="cerrarImagenModal"
            >
              Cerrar
            </button>

            <p v-if="cargandoImagen" class="imagen-modal__loading">Cargando imagen...</p>
            <img
              v-else-if="imagenUrl"
              :src="imagenUrl"
              alt="Imagen del DNI del menor"
              class="imagen-modal__img"
            />
          </div>
        </div>
      </Teleport>
    </div>
  </section>
</template>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
  flex-wrap: wrap;
}

.imagen-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.imagen-modal__backdrop {
  position: absolute;
  inset: 0;
  border: none;
  background: rgba(17, 17, 17, 0.82);
  cursor: pointer;
}

.imagen-modal__content {
  position: relative;
  z-index: 1;
  max-width: min(960px, 100%);
  max-height: min(90vh, 100%);
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.75rem;
}

.imagen-modal__loading {
  color: var(--cip-white);
  font-weight: 600;
  align-self: center;
  padding: 2rem;
}

.imagen-modal__img {
  display: block;
  max-width: min(960px, 100%);
  max-height: calc(90vh - 3rem);
  width: auto;
  height: auto;
  object-fit: contain;
  background: var(--cip-white);
  border: 1px solid var(--color-border);
}

.rechazo {
  margin-top: 0.75rem;
  display: grid;
  gap: 0.5rem;
  min-width: 220px;
}

.rechazo label {
  font-weight: 600;
  font-size: 0.88rem;
}

.rechazo textarea {
  width: 100%;
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--cip-white);
  color: var(--color-text);
  resize: vertical;
}

.rechazo__acciones {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.solicitudes-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 1.25rem;
}

.solicitudes-tabs__tab {
  appearance: none;
  border: none;
  background: transparent;
  padding: 0.75rem 1.25rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-text-muted, #666);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.solicitudes-tabs__tab:hover {
  color: var(--color-text);
}

.solicitudes-tabs__tab--active {
  color: var(--color-text);
  border-bottom-color: var(--cip-red, #c8102e);
}

.solicitudes-tabs__count {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--color-text-muted, #666);
  background: var(--color-surface-alt, #f0f0f0);
  padding: 0.1rem 0.45rem;
  border-radius: 999px;
}

.solicitudes-tabs__tab--active .solicitudes-tabs__count {
  color: var(--color-text);
}

.motivo-cell {
  max-width: 220px;
  white-space: normal;
  word-break: break-word;
}
</style>
