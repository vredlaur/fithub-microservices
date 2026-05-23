import { createContext, useContext, useMemo, useState } from 'react'
import { http } from '../api/http.js'

const AuthContext = createContext(null)

function storedUser() {
  const raw = localStorage.getItem('fithub.user')
  return raw ? JSON.parse(raw) : null
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(storedUser)

  async function login(payload) {
    const { data } = await http.post('/auth/login', payload)
    localStorage.setItem('fithub.token', data.token)
    localStorage.setItem('fithub.user', JSON.stringify(data))
    setUser(data)
    return data
  }

  async function register(payload) {
    const { data } = await http.post('/auth/register', payload)
    localStorage.setItem('fithub.token', data.token)
    localStorage.setItem('fithub.user', JSON.stringify(data))
    setUser(data)
    return data
  }

  function logout() {
    localStorage.removeItem('fithub.token')
    localStorage.removeItem('fithub.user')
    setUser(null)
  }

  const value = useMemo(() => ({ user, login, register, logout }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth trebuie folosit in AuthProvider.')
  }
  return context
}
