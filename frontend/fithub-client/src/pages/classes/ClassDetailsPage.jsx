import { CalendarCheck } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useParams } from 'react-router-dom'
import { errorMessage, http } from '../../api/http.js'

export function ClassDetailsPage() {
  const { id } = useParams()
  const [fitnessClass, setFitnessClass] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const { register, handleSubmit, formState } = useForm({ defaultValues: { clientId: 1 } })

  const load = useCallback(async () => {
    const { data } = await http.get(`/classes/${id}`)
    setFitnessClass(data)
  }, [id])

  useEffect(() => {
    load().catch((exception) => setError(errorMessage(exception)))
  }, [load])

  async function reserve(values) {
    setError('')
    setMessage('')
    try {
      await http.post('/bookings', { clientId: Number(values.clientId), fitnessClassId: Number(id) })
      setMessage('Rezervarea a fost confirmata.')
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  if (!fitnessClass) return <div className="panel">Se incarca...</div>

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">{fitnessClass.name}</h1>
        <div className="muted">{fitnessClass.classType?.name} · {fitnessClass.status}</div>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="row g-3">
        <div className="col-12 col-lg-7">
          <section className="panel">
            <dl className="row mb-0">
              <dt className="col-sm-4">Start</dt>
              <dd className="col-sm-8">{fitnessClass.startTime?.replace('T', ' ').slice(0, 16)}</dd>
              <dt className="col-sm-4">Final</dt>
              <dd className="col-sm-8">{fitnessClass.endTime?.replace('T', ' ').slice(0, 16)}</dd>
              <dt className="col-sm-4">Antrenor</dt>
              <dd className="col-sm-8">{fitnessClass.trainer?.firstName} {fitnessClass.trainer?.lastName}</dd>
              <dt className="col-sm-4">Sala</dt>
              <dd className="col-sm-8">{fitnessClass.trainingRoom?.name}</dd>
              <dt className="col-sm-4">Locuri</dt>
              <dd className="col-sm-8">{fitnessClass.availableSlots} din {fitnessClass.capacity}</dd>
            </dl>
          </section>
        </div>
        <div className="col-12 col-lg-5">
          <form className="panel" onSubmit={handleSubmit(reserve)}>
            <h2 className="h5 fw-bold">Rezervare</h2>
            <label className="form-label">ID client</label>
            <input className="form-control" type="number" {...register('clientId', { required: 'Client obligatoriu.' })} />
            <div className="text-danger small">{formState.errors.clientId?.message}</div>
            <button className="btn btn-brand mt-3" type="submit" disabled={formState.isSubmitting}>
              <CalendarCheck size={16} /> Rezerva loc
            </button>
          </form>
        </div>
      </div>
    </>
  )
}
