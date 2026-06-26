<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/services/api'
import {
  aprobarSolicitud,
  listarPendientes,
  obtenerImagenSolicitud,
  obtenerMetricas,
  rechazarSolicitud,
} from '@/services/adminService'
import { logout } from '@/services/authService'

const router = useRouter()

const metricas = ref(null)
const pendientes = ref([])
const cargando = ref(true)
const error = ref('')
const mensaje = ref('')

const imagenUrl = ref('')
const imagenSolicitudId = ref(null)
const cargandoImagen = ref(false)

const rechazoActivoId = ref(null)
const observacionRechazo = ref('')
const procesandoId = ref(null)

onMounted(async () => {
  await cargarPanel()
})

onBeforeUnmount(() => {
  revocarImagen()
})

async function cargarPanel() {
  cargando.value = true
  error.value = ''

  try {
    const [metricasData, pendientesData] = await Promise.all([
      obtenerMetricas(),
      listarPendientes(),
    ])
    metricas.value = metricasData
    pendientes.value = pendientesData
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

        <section class="cip-card">
          <h2 class="section-title">Solicitudes pendientes</h2>

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
        </section>

        <section v-if="cargandoImagen || imagenUrl" class="cip-card imagen-panel">
          <h2 class="section-title">Imagen del DNI del menor</h2>
          <p v-if="cargandoImagen">Cargando imagen...</p>
          <img
            v-else-if="imagenUrl"
            :src="imagenUrl"
            alt="Imagen del DNI del menor"
            class="imagen-panel__img"
          />
          <button
            v-if="imagenUrl"
            type="button"
            class="cip-btn cip-btn--secondary cip-btn--small"
            @click="revocarImagen"
          >
            Cerrar imagen
          </button>
        </section>
      </template>
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

.section-title {
  font-size: 1rem;
  color: var(--color-heading);
  margin-bottom: 1rem;
  font-weight: 700;
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

.imagen-panel__img {
  display: block;
  max-width: 100%;
  max-height: 420px;
  margin: 0.75rem 0;
  border: 1px solid var(--color-border);
  border-radius: 4px;
}
</style>
