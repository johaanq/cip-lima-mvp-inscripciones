export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function apiFetch(path, options = {}) {
  const response = await fetch(`${apiBaseUrl}${path}`, options)

  if (response.ok) {
    if (response.status === 204) {
      return null
    }
    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      return response.json()
    }
    return response
  }

  let message = 'Ocurrió un error al procesar la solicitud'
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
