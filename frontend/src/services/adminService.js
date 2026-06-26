import { apiBaseUrl, apiFetch, ApiError } from './api'
import { getAuthToken } from './authSession'

export function obtenerMetricas() {
  return apiFetch('/admin/metricas')
}

export function listarPendientes() {
  return apiFetch('/admin/solicitudes/pendientes')
}

export function listarAprobadas() {
  return apiFetch('/admin/solicitudes/aprobadas')
}

export function listarRechazadas() {
  return apiFetch('/admin/solicitudes/rechazadas')
}

export function aprobarSolicitud(id) {
  return apiFetch(`/admin/solicitudes/${id}/aprobar`, {
    method: 'POST',
  })
}

export function rechazarSolicitud(id, observacion) {
  return apiFetch(`/admin/solicitudes/${id}/rechazar`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ observacion }),
  })
}

export async function obtenerImagenSolicitud(id) {
  const response = await fetch(`${apiBaseUrl}/admin/solicitudes/${id}/imagen`, {
    headers: {
      Authorization: `Bearer ${getAuthToken()}`,
    },
  })

  if (!response.ok) {
    let message = 'No fue posible obtener la imagen'
    try {
      const body = await response.json()
      if (body?.error) {
        message = body.error
      }
    } catch {
      // respuesta no JSON
    }
    throw new ApiError(message, response.status)
  }

  const blob = await response.blob()
  return URL.createObjectURL(blob)
}
