import { CalendarDays, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage, http } from '../../api/http.js'

export function ClassesPage() {
  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [sort, setSort] = useState('startTime,asc')
  const [error, setError] = useState('')

  useEffect(() => {
    http.get('/classes', { params: { page, size: 9, sort } })
      .then(({ data }) => {
        setItems(data.content || [])
        setTotalPages(data.totalPages || 1)
      })
      .catch((exception) => setError(errorMessage(exception)))
  }, [page, sort])

  return (
    <>
      <div className="d-flex justify-content-between align-items-end mb-3 gap-2 flex-wrap">
        <div>
          <h1 className="page-title">Clase fitness</h1>
          <div className="muted">Alege o clasa si confirma rezervarea.</div>
        </div>
        <select className="form-select w-auto" value={sort} onChange={(event) => setSort(event.target.value)}>
          <option value="startTime,asc">data asc</option>
          <option value="startTime,desc">data desc</option>
          <option value="name,asc">nume asc</option>
          <option value="name,desc">nume desc</option>
        </select>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="row g-3">
        {items.map((item) => (
          <div className="col-12 col-md-6 col-xl-4" key={item.id}>
            <article className="panel h-100">
              <div className="d-flex justify-content-between gap-2">
                <h2 className="h5 fw-bold">{item.name}</h2>
                <span className="badge text-bg-success">{item.availableSlots}/{item.capacity}</span>
              </div>
              <div className="muted small mb-2">{item.classType?.name} - {item.trainer?.firstName} {item.trainer?.lastName}</div>
              <div className="d-flex align-items-center gap-2 mb-3">
                <CalendarDays size={16} />
                <span>{item.startTime?.replace('T', ' ').slice(0, 16)}</span>
              </div>
              <Link className="btn btn-outline-success btn-sm" to={`/classes/${item.id}`}>
                <Search size={15} /> Detalii
              </Link>
            </article>
          </div>
        ))}
      </div>
      <div className="btn-group mt-3">
        <button className="btn btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
        <button className="btn btn-outline-secondary" disabled>{page + 1}/{totalPages}</button>
        <button className="btn btn-outline-secondary" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </>
  )
}
