<script setup>
import { computed, onMounted, ref } from 'vue'
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

const inscripcionesAbiertas = computed(() => estadoEvento.value?.inscripcionesAbiertas ?? false)

const puedeEnviar = computed(() =>
  inscripcionesAbiertas.value &&
  !enviando.value &&
  !cargandoEvento.value
)

onMounted(async () => {
  await cargarEstadoEvento()
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
</script>

<template>
  <section class="inscripcion">
    <header class="inscripcion__header">
      <p class="inscripcion__eyebrow">CIP Lima</p>
      <h1>Inscripción al evento Día del Padre</h1>
      <p class="inscripcion__descripcion">
        Complete el formulario para registrar la participación de su hijo menor.
        La solicitud será revisada por el equipo administrativo del consejo.
      </p>
    </header>

    <div v-if="cargandoEvento" class="panel panel--info">
      Consultando disponibilidad del evento...
    </div>

    <div v-else-if="errorEvento" class="panel panel--error">
      {{ errorEvento }}
      <button type="button" class="btn btn--secondary" @click="cargarEstadoEvento">
        Reintentar
      </button>
    </div>

    <template v-else-if="estadoEvento">
      <div class="estado-evento">
        <div>
          <span class="estado-evento__label">Sede</span>
          <strong>{{ estadoEvento.sedeConsejo }}</strong>
        </div>
        <div>
          <span class="estado-evento__label">Cupo disponible</span>
          <strong>{{ estadoEvento.cupoDisponible }} / {{ estadoEvento.cupoMaximo }}</strong>
        </div>
      </div>

      <div v-if="!inscripcionesAbiertas" class="panel panel--warning">
        El evento ha alcanzado su aforo máximo. No se aceptan nuevas inscripciones.
      </div>

      <form v-else class="formulario" @submit.prevent="enviarInscripcion">
        <div class="campo">
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

        <div class="campo">
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

        <div class="campo">
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

        <div class="campo">
          <label for="imagen">Imagen del DNI del menor</label>
          <input
            id="imagen"
            type="file"
            accept="image/jpeg,image/png,image/jpg"
            :disabled="!puedeEnviar"
            @change="onArchivoSeleccionado"
          />
          <p class="campo__ayuda">Formatos permitidos: JPG o PNG. Tamaño máximo 5 MB.</p>
        </div>

        <p v-if="errorFormulario" class="formulario__error">{{ errorFormulario }}</p>

        <button type="submit" class="btn btn--primary" :disabled="!puedeEnviar">
          {{ enviando ? 'Enviando...' : 'Registrar inscripción' }}
        </button>
      </form>

      <div
        v-if="resultado"
        class="panel"
        :class="{
          'panel--success': resultado.estado === 'PENDIENTE' || resultado.estado === 'APROBADO',
          'panel--error': resultado.estado === 'RECHAZADO',
        }"
      >
        <h2>Resultado de la solicitud</h2>
        <p><strong>ID:</strong> {{ resultado.id }}</p>
        <p><strong>Estado:</strong> {{ resultado.estado }}</p>
        <p>{{ resultado.mensaje }}</p>
        <p v-if="resultado.motivoRechazo">
          <strong>Motivo:</strong> {{ resultado.motivoRechazo }}
        </p>
        <button type="button" class="btn btn--secondary" @click="reiniciarFormulario">
          Nueva inscripción
        </button>
      </div>
    </template>
  </section>
</template>

<style scoped>
.inscripcion {
  width: min(640px, 100%);
  margin: 0 auto;
}

.inscripcion__header {
  margin-bottom: 1.5rem;
}

.inscripcion__eyebrow {
  font-size: 0.85rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: hsla(160, 100%, 30%, 1);
  margin-bottom: 0.5rem;
}

.inscripcion__header h1 {
  font-size: 1.75rem;
  color: var(--color-heading);
  margin-bottom: 0.75rem;
}

.inscripcion__descripcion {
  color: var(--color-text);
}

.estado-evento {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-background-soft);
}

.estado-evento__label {
  display: block;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
  color: var(--color-text);
}

.formulario {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.campo {
  display: grid;
  gap: 0.35rem;
}

.campo label {
  font-weight: 600;
  color: var(--color-heading);
}

.campo input[type='text'],
.campo input[type='file'] {
  width: 100%;
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-background);
  color: var(--color-text);
}

.campo__ayuda {
  font-size: 0.85rem;
  color: var(--color-text);
}

.formulario__error {
  color: #b42318;
}

.panel {
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  margin-bottom: 1rem;
}

.panel h2 {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
}

.panel--info {
  background: var(--color-background-soft);
}

.panel--warning {
  background: #fff7ed;
  border-color: #fdba74;
  color: #9a3412;
}

.panel--success {
  background: #ecfdf3;
  border-color: #86efac;
  color: #166534;
}

.panel--error {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #991b1b;
}

.btn {
  margin-top: 0.75rem;
  padding: 0.65rem 1rem;
  border-radius: 6px;
  border: 1px solid transparent;
  cursor: pointer;
  font-weight: 600;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn--primary {
  background: hsla(160, 100%, 37%, 1);
  color: #fff;
}

.btn--secondary {
  background: transparent;
  border-color: var(--color-border);
  color: var(--color-heading);
}
</style>
