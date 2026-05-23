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
  email: z.string().email('Email invalid.'),
  password: z.string().min(8, 'Parola trebuie sa aiba minimum 8 caractere.'),
  lastName: z.string().min(1, 'Numele este obligatoriu.'),
  firstName: z.string().min(1, 'Prenumele este obligatoriu.'),
  phone: z.string().optional(),
})

export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  async function onSubmit(values) {
    setError('')
    try {
      await registerUser(values)
      navigate('/subscriptions')
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
        <h1 className="h3 fw-bold mb-3">Cont nou</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        <div className="row g-2">
          {[
            ['username', 'Username'],
            ['email', 'Email'],
            ['password', 'Parola', 'password'],
            ['lastName', 'Nume'],
            ['firstName', 'Prenume'],
            ['phone', 'Telefon'],
          ].map(([name, label, type]) => (
            <div className="col-12 col-md-6" key={name}>
              <label className="form-label">{label}</label>
              <input className="form-control" type={type || 'text'} {...register(name)} />
              <div className="text-danger small">{formState.errors[name]?.message}</div>
            </div>
          ))}
        </div>
        <button className="btn btn-brand w-100 mt-3" type="submit" disabled={formState.isSubmitting}>
          Inregistrare
        </button>
        <div className="small text-center mt-3">
          Ai deja cont? <Link to="/login">Login</Link>
        </div>
      </form>
    </div>
  )
}
