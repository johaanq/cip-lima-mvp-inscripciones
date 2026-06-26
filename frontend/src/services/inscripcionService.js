import { apiFetch } from './api'

export function obtenerEstadoEvento() {
  return apiFetch('/evento/estado')
}

export function registrarInscripcion({ dniColegiado, nombreColegiado, dniMenor, imagen }) {
  const formData = new FormData()
  formData.append('dniColegiado', dniColegiado)
  formData.append('nombreColegiado', nombreColegiado)
  formData.append('dniMenor', dniMenor)
  formData.append('imagen', imagen)

  return apiFetch('/inscripciones', {
    method: 'POST',
    body: formData,
  })
}

export function consultarInscripcion(id) {
  return apiFetch(`/inscripciones/${id}`)
}
