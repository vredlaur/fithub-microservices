import { CalendarCheck } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage, http } from '../../api/http.js'

export function ClassDetailsPage() {
  const { id } = useParams()
  const [fitnessClass, setFitnessClass] = useState(null)
  const [currentClient, setCurrentClient] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    const classResponse = await http.get(`/classes/${id}`)
    setFitnessClass(classResponse.data)
    try {
      const clientResponse = await http.get('/clients/me')
      setCurrentClient(clientResponse.data)
    } catch {
      setCurrentClient(null)
    }
  }, [id])

  useEffect(() => {
    load().catch((exception) => setError(errorMessage(exception)))
  }, [load])

  async function reserve() {
    setError('')
    setMessage('')
    setSubmitting(true)
    try {
      await http.post('/bookings/me', { fitnessClassId: Number(id) })
      setMessage('Rezervarea a fost confirmata.')
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    } finally {
      setSubmitting(false)
    }
  }

  if (!fitnessClass) return <div className="panel">Se incarca...</div>

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">{fitnessClass.name}</h1>
        <div className="muted">{fitnessClass.classType?.name} - {fitnessClass.status}</div>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      {error && (
        <div className="alert alert-danger">
          {error}
          {error.toLowerCase().includes('abonament') && (
            <div className="mt-2">
              <Link className="btn btn-outline-danger btn-sm" to="/subscriptions">Alege abonament</Link>
            </div>
          )}
        </div>
      )}
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
          <section className="panel">
            <h2 className="h5 fw-bold">Rezervare</h2>
            <div className="mb-3">
              <div className="form-label">Client</div>
              <div className="form-control bg-light">
                {currentClient ? `${currentClient.firstName} ${currentClient.lastName}` : 'Profilul de client nu este incarcat'}
              </div>
            </div>
            <button className="btn btn-brand mt-3" type="button" onClick={reserve} disabled={submitting || !currentClient}>
              <CalendarCheck size={16} /> Rezerva loc
            </button>
            {!currentClient && (
              <div className="mt-3">
                <Link className="btn btn-outline-secondary btn-sm" to="/subscriptions">Mergi la abonamente</Link>
              </div>
            )}
          </section>
        </div>
      </div>
    </>
  )
}
