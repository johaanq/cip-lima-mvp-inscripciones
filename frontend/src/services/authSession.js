let authToken = null

export function setAuthToken(token) {
  authToken = token
}

export function clearAuthToken() {
  authToken = null
}

export function getAuthToken() {
  return authToken
}

export function isAuthenticated() {
  return Boolean(authToken)
}
