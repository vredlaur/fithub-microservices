import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fithub.token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function errorMessage(error) {
  return error?.response?.data?.message || 'A aparut o eroare. Verifica serviciile backend.'
}
