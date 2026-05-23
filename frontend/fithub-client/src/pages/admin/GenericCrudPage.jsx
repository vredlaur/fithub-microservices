import { Edit, Plus, Trash2, X } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { errorMessage, http } from '../../api/http.js'

function display(value) {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'object') return value.name || value.email || value.id || JSON.stringify(value)
  if (typeof value === 'boolean') return value ? 'Da' : 'Nu'
  return String(value)
}

function initialValues(fields) {
  return Object.fromEntries(fields.map((field) => [field.name, field.type === 'checkbox' ? true : '']))
}

function flatten(entity, fields) {
  const values = {}
  for (const field of fields) {
    const value = entity[field.name]
    if (field.type === 'relation') values[field.name] = value?.id || ''
    else if (field.type === 'datetime-local') values[field.name] = value ? value.slice(0, 16) : ''
    else if (field.name === 'roles') values[field.name] = value?.map?.((role) => role.name || role).join(',') || ''
    else values[field.name] = value ?? (field.type === 'checkbox' ? false : '')
  }
  return values
}

function payload(values, fields) {
  const result = {}
  for (const field of fields) {
    const value = values[field.name]
    if (field.type === 'relation') result[field.name] = value ? { id: Number(value) } : null
    else if (field.type === 'number') result[field.name] = value === '' ? null : Number(value)
    else if (field.type === 'checkbox') result[field.name] = Boolean(value)
    else if (field.name === 'roles') result[field.name] = value ? value.split(',').map((role) => role.trim()).filter(Boolean) : ['USER']
    else result[field.name] = value
  }
  return result
}

export function GenericCrudPage({ config }) {
  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [sort, setSort] = useState(`${config.sortOptions[0]},asc`)
  const [editing, setEditing] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const { register, handleSubmit, reset, formState } = useForm({ defaultValues: initialValues(config.fields) })

  const columns = useMemo(() => ['id', ...config.fields.slice(0, 5).map((field) => field.name)], [config.fields])

  const load = useCallback(async () => {
    setError('')
    try {
      const { data } = await http.get(config.endpoint, { params: { page, size: 8, sort } })
      setItems(data.content || data)
      setTotalPages(data.totalPages || 1)
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }, [config.endpoint, page, sort])

  useEffect(() => {
    reset(initialValues(config.fields))
    setEditing(null)
    setPage(0)
  }, [config, reset])

  useEffect(() => {
    load()
  }, [load])

  async function onSubmit(values) {
    setError('')
    setMessage('')
    try {
      const body = payload(values, config.fields)
      if (editing) await http.put(`${config.endpoint}/${editing.id}`, body)
      else await http.post(config.endpoint, body)
      reset(initialValues(config.fields))
      setEditing(null)
      setMessage('Modificarile au fost salvate.')
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  async function onDelete(item) {
    if (!window.confirm(`Stergi inregistrarea #${item.id}?`)) return
    try {
      await http.delete(`${config.endpoint}/${item.id}`)
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    }
  }

  function onEdit(item) {
    setEditing(item)
    reset(flatten(item, config.fields))
  }

  function cancelEdit() {
    setEditing(null)
    reset(initialValues(config.fields))
  }

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">{config.title}</h1>
        <div className="muted">CRUD cu paginare si sortare prin API Gateway.</div>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="crud-grid">
        <section className="panel">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <select className="form-select form-select-sm w-auto" value={sort} onChange={(event) => setSort(event.target.value)}>
              {config.sortOptions.flatMap((option) => [
                <option key={`${option}-asc`} value={`${option},asc`}>{option} asc</option>,
                <option key={`${option}-desc`} value={`${option},desc`}>{option} desc</option>,
              ])}
            </select>
            <div className="btn-group btn-group-sm">
              <button className="btn btn-outline-secondary" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Prev</button>
              <button className="btn btn-outline-secondary" disabled>{page + 1}/{totalPages}</button>
              <button className="btn btn-outline-secondary" disabled={page + 1 >= totalPages} onClick={() => setPage((value) => value + 1)}>Next</button>
            </div>
          </div>
          <div className="table-wrap">
            <table className="table table-sm align-middle">
              <thead>
                <tr>
                  {columns.map((column) => <th key={column}>{column}</th>)}
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    {columns.map((column) => <td key={column}>{display(item[column])}</td>)}
                    <td className="text-end">
                      <button className="btn btn-outline-secondary btn-sm me-1" onClick={() => onEdit(item)} title="Editare">
                        <Edit size={15} />
                      </button>
                      <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(item)} title="Stergere">
                        <Trash2 size={15} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
        <form className="panel" onSubmit={handleSubmit(onSubmit)}>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="h5 fw-bold mb-0">{editing ? `Editare #${editing.id}` : 'Inregistrare noua'}</h2>
            {editing && <button className="btn btn-outline-secondary btn-sm" type="button" onClick={cancelEdit}><X size={15} /></button>}
          </div>
          <div className="form-grid">
            {config.fields.map((field) => (
              <div key={field.name} className={field.type === 'checkbox' ? 'span-2 form-check mt-2' : ''}>
                {field.type === 'checkbox' ? (
                  <>
                    <input className="form-check-input" type="checkbox" {...register(field.name)} />
                    <label className="form-check-label">{field.label}</label>
                  </>
                ) : (
                  <>
                    <label className="form-label">{field.label}</label>
                    <input
                      className="form-control"
                      type={field.type || 'text'}
                      placeholder={field.placeholder || ''}
                      {...register(field.name, { required: field.required ? 'Camp obligatoriu.' : false })}
                    />
                    <div className="text-danger small">{formState.errors[field.name]?.message}</div>
                  </>
                )}
              </div>
            ))}
          </div>
          <button className="btn btn-brand mt-3" type="submit" disabled={formState.isSubmitting}>
            <Plus size={16} /> Salveaza
          </button>
        </form>
      </div>
    </>
  )
}
