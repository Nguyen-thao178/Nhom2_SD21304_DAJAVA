import { reactive } from 'vue'
import { api, clearCsrf } from '../api/client'
export const session = reactive({ user: null })
export async function refreshSession() {
  try { session.user = await api('/api/v1/auth/me'); return session.user }
  catch (error) { if (error.status === 401) { session.user = null; return null } throw error }
}
export async function login(username, password) {
  session.user = await api('/api/v1/auth/login', { method: 'POST', body: { username, password } }); clearCsrf(); return session.user
}
export async function logout() {
  await api('/api/v1/auth/logout', { method: 'POST' }); session.user = null; clearCsrf()
}
