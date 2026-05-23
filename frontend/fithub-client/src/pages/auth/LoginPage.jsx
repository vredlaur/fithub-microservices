import { zodResolver } from '@hookform/resolvers/zod'
import { Dumbbell } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { errorMessage } from '../../api/http.js'
import { useAuth } from '../../hooks/useAuth.js'

const schema = z.object({
  username: z.string().min(1, 'Username-ul este obligatoriu.'),
  password: z.string().min(8, 'Parola trebuie sa aiba minimum 8 caractere.'),
})

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { username: 'admin', password: 'Admin123!' },
  })

  async function onSubmit(values) {
    setError('')
    try {
      await login(values)
      navigate('/dashboard')
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-panel" onSubmit={handleSubmit(onSubmit)}>
        <div className="brand text-dark mb-3">
          <Dumbbell />
          FitHub
        </div>
        <h1 className="h3 fw-bold mb-3">Autentificare</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        <label className="form-label">Username</label>
        <input className="form-control mb-1" {...register('username')} />
        <div className="text-danger small mb-2">{formState.errors.username?.message}</div>
        <label className="form-label">Parola</label>
        <input className="form-control mb-1" type="password" {...register('password')} />
        <div className="text-danger small mb-3">{formState.errors.password?.message}</div>
        <button className="btn btn-brand w-100" type="submit" disabled={formState.isSubmitting}>
          Login
        </button>
        <div className="small text-center mt-3">
          Nu ai cont? <Link to="/register">Creeaza cont</Link>
        </div>
      </form>
    </div>
  )
}
