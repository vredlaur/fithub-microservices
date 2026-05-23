import { Trash2 } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { errorMessage, http } from '../../api/http.js'

function classLabel(item) {
  if (!item) return ''
  const date = item.startTime ? item.startTime.replace('T', ' ').slice(0, 16) : ''
  return `${item.name} - ${date} - ${item.availableSlots}/${item.capacity} locuri`
}

export function BookingsPage() {
  const [items, setItems] = useState([])
  const [classes, setClasses] = useState([])
  const [currentClient, setCurrentClient] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [sort, setSort] = useState('bookingDate,desc')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const { register, handleSubmit, reset, formState } = useForm({ defaultValues: { fitnessClassId: '' } })

  const classById = useMemo(() => new Map(classes.map((item) => [item.id, item])), [classes])

  const loadBookings = useCallback(async () => {
    const { data } = await http.get('/bookings/me', { params: { page, size: 8, sort } })
    setItems(data.content || [])
    setTotalPages(data.totalPages || 1)
  }, [page, sort])

  const loadContext = useCallback(async () => {
    const [clientResponse, classesResponse] = await Promise.all([
      http.get('/clients/me'),
      http.get('/classes', { params: { page: 0, size: 100, sort: 'startTime,asc' } }),
    ])
    const loadedClasses = classesResponse.data.content || []
    setCurrentClient(clientResponse.data)
    setClasses(loadedClasses)
    reset({ fitnessClassId: loadedClasses[0] ? String(loadedClasses[0].id) : '' })
  }, [reset])

  useEffect(() => {
    setError('')
    Promise.all([loadBookings(), loadContext()]).catch((exception) => setError(errorMessage(exception)))
  }, [loadBookings, loadContext])

  async function create(values) {
    setError('')
    setMessage('')
    try {
      if (!currentClient) throw new Error('Clientul contului curent nu este incarcat.')
      await http.post('/bookings/me', {
        fitnessClassId: Number(values.fitnessClassId),
      })
      reset({ fitnessClassId: values.fitnessClassId })
      setMessage('Rezervarea a fost creata.')
      await Promise.all([loadBookings(), loadContext()])
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  async function remove(item) {
    if (!window.confirm(`Anulezi rezervarea #${item.id}?`)) return
    try {
      await http.delete(`/bookings/me/${item.id}`)
      await Promise.all([loadBookings(), loadContext()])
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">Rezervari</h1>
        <div className="muted">Rezervarile sunt legate automat de contul autentificat.</div>
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
      <div className="crud-grid">
        <section className="panel">
          <div className="d-flex justify-content-between mb-2">
            <select className="form-select form-select-sm w-auto" value={sort} onChange={(event) => setSort(event.target.value)}>
              <option value="bookingDate,desc">data desc</option>
              <option value="bookingDate,asc">data asc</option>
              <option value="status,asc">status asc</option>
              <option value="status,desc">status desc</option>
            </select>
          </div>
          <table className="table table-sm align-middle">
            <thead><tr><th>ID</th><th>Client</th><th>Clasa</th><th>Data</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {items.length === 0 && (
                <tr><td colSpan="6">Nu exista rezervari.</td></tr>
              )}
              {items.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.client?.firstName} {item.client?.lastName}</td>
                  <td>{classById.get(item.fitnessClassId)?.name || `#${item.fitnessClassId}`}</td>
                  <td>{item.bookingDate?.replace('T', ' ').slice(0, 16)}</td>
                  <td>{item.status}</td>
                  <td className="text-end">
                    <button className="btn btn-outline-danger btn-sm" onClick={() => remove(item)}><Trash2 size={15} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="btn-group btn-group-sm">
            <button className="btn btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
            <button className="btn btn-outline-secondary" disabled>{page + 1}/{totalPages}</button>
            <button className="btn btn-outline-secondary" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</button>
          </div>
        </section>
        <form className="panel" onSubmit={handleSubmit(create)}>
          <h2 className="h5 fw-bold">Rezervare noua</h2>
          <div className="mb-3">
            <div className="form-label">Client</div>
            <div className="form-control bg-light">
              {currentClient ? `${currentClient.firstName} ${currentClient.lastName}` : 'Se incarca...'}
            </div>
          </div>
          <label className="form-label">Clasa fitness</label>
          <select className="form-select" {...register('fitnessClassId', { required: 'Clasa este obligatorie.' })}>
            <option value="">Alege clasa...</option>
            {classes.map((item) => (
              <option key={item.id} value={item.id}>{classLabel(item)}</option>
            ))}
          </select>
          <div className="text-danger small">{formState.errors.fitnessClassId?.message}</div>
          <button className="btn btn-brand mt-3" type="submit" disabled={formState.isSubmitting || !currentClient}>Rezerva</button>
        </form>
      </div>
    </>
  )
}
