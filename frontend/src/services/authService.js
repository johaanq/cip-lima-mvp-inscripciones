import { apiFetch } from './api'
import { clearAuthToken, setAuthToken } from './authSession'

export async function login(username, password) {
  const response = await apiFetch('/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  })

  setAuthToken(response.token)
  return response
}

export function logout() {
  clearAuthToken()
}
