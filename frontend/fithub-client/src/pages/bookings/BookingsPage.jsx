import { Trash2 } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { errorMessage, http } from '../../api/http.js'

export function BookingsPage() {
  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [sort, setSort] = useState('bookingDate,desc')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const { register, handleSubmit, reset, formState } = useForm({ defaultValues: { clientId: 1, fitnessClassId: 1 } })

  const load = useCallback(async () => {
    const { data } = await http.get('/bookings', { params: { page, size: 8, sort } })
    setItems(data.content || [])
    setTotalPages(data.totalPages || 1)
  }, [page, sort])

  useEffect(() => {
    load().catch((exception) => setError(errorMessage(exception)))
  }, [load])

  async function create(values) {
    setError('')
    setMessage('')
    try {
      await http.post('/bookings', { clientId: Number(values.clientId), fitnessClassId: Number(values.fitnessClassId) })
      reset({ clientId: values.clientId, fitnessClassId: '' })
      setMessage('Rezervarea a fost creata.')
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  async function remove(item) {
    if (!window.confirm(`Anulezi rezervarea #${item.id}?`)) return
    await http.delete(`/bookings/${item.id}`)
    await load()
  }

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">Rezervari</h1>
        <div className="muted">Rezervarile folosesc booking-service si gym-service prin Feign.</div>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}
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
              {items.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.client?.firstName} {item.client?.lastName}</td>
                  <td>#{item.fitnessClassId}</td>
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
          <label className="form-label">ID client</label>
          <input className="form-control mb-2" type="number" {...register('clientId', { required: true })} />
          <label className="form-label">ID clasa fitness</label>
          <input className="form-control" type="number" {...register('fitnessClassId', { required: true })} />
          <button className="btn btn-brand mt-3" type="submit" disabled={formState.isSubmitting}>Rezerva</button>
        </form>
      </div>
    </>
  )
}
