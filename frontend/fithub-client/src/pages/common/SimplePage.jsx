import { Check } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { errorMessage, http } from '../../api/http.js'

export function SimplePage({ title, endpoint, markRead, icon: Icon }) {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    const { data } = await http.get(endpoint, { params: { page: 0, size: 20, sort: 'id,desc' } })
    setItems(data.content || data)
  }, [endpoint])

  useEffect(() => {
    load().catch((exception) => setError(errorMessage(exception)))
  }, [load])

  async function read(id) {
    await http.put(`${endpoint}/${id}/read`)
    await load()
  }

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">{title}</h1>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      <section className="panel">
        <table className="table table-sm align-middle">
          <thead><tr><th>ID</th><th>Detalii</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td>
                  <div className="d-flex align-items-center gap-2">
                    {Icon && <Icon size={16} />}
                    <span>{item.title || item.subscriptionType?.name || item.status || item.name || `#${item.id}`}</span>
                  </div>
                  <div className="muted small">{item.message || item.startDate || item.createdAt}</div>
                </td>
                <td>{item.read ? 'Citita' : item.status || '-'}</td>
                <td className="text-end">
                  {markRead && !item.read && (
                    <button className="btn btn-outline-success btn-sm" onClick={() => read(item.id)}>
                      <Check size={15} />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  )
}
