import { Edit, Plus, Trash2, X } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { errorMessage, http } from '../../api/http.js'

function extractItems(data) {
  return data?.content || data || []
}

function optionText(value, field = {}) {
  if (value === null || value === undefined || value === '') return '-'
  if (Array.isArray(value)) return value.map((item) => optionText(item, field)).join(', ')
  if (typeof value !== 'object') return String(value)

  const labelConfig = field.optionLabel
  if (Array.isArray(labelConfig)) {
    const text = labelConfig
      .map((key) => (key === 'id' ? `#${value[key]}` : value[key]))
      .filter((part) => part !== undefined && part !== null && part !== '')
      .join(' - ')
    if (text) return text
  }

  if (typeof labelConfig === 'string' && value[labelConfig]) return String(value[labelConfig])
  return value.name || value.email || value.username || value.id || JSON.stringify(value)
}

function display(value, field) {
  if (typeof value === 'boolean') return value ? 'Da' : 'Nu'
  return optionText(value, field)
}

function getByPath(value, path) {
  if (!path) return undefined
  return path.split('.').reduce((current, key) => current?.[key], value)
}

function initialValues(fields) {
  return Object.fromEntries(fields.map((field) => {
    if (field.type === 'checkbox') return [field.name, true]
    if (field.type === 'relationMulti') return [field.name, []]
    return [field.name, '']
  }))
}

function relationValue(value, field) {
  if (!value) return ''
  if (typeof value !== 'object') return value
  const key = field.valueKey || 'id'
  return value[key] ?? ''
}

function flatten(entity, fields) {
  const values = {}
  for (const field of fields) {
    const value = getByPath(entity, field.path || field.name)
    if (field.type === 'relation') values[field.name] = relationValue(value, field)
    else if (field.type === 'relationMulti') values[field.name] = Array.isArray(value) ? value.map((item) => relationValue(item, field)) : []
    else if (field.type === 'datetime-local') values[field.name] = value ? value.slice(0, 16) : ''
    else values[field.name] = value ?? (field.type === 'checkbox' ? false : '')
  }
  return values
}

function payload(values, fields) {
  const result = {}
  for (const field of fields) {
    const value = values[field.name]
    if (field.omitWhenEmpty && (value === '' || value === null || value === undefined)) {
      continue
    }
    if (field.type === 'relation') {
      result[field.name] = value ? { id: Number(value) } : null
    } else if (field.type === 'relationMulti') {
      const list = Array.isArray(value) ? value : [value].filter(Boolean)
      result[field.name] = field.valueKey === 'name' ? list : list.map((id) => ({ id: Number(id) }))
    } else if (field.type === 'number') {
      result[field.name] = value === '' ? null : Number(value)
    } else if (field.type === 'checkbox') {
      result[field.name] = Boolean(value)
    } else if ((field.type === 'date' || field.type === 'datetime-local') && value === '') {
      result[field.name] = null
    } else {
      result[field.name] = typeof value === 'string' ? value.trim() : value
    }
  }
  return result
}

function inputType(field) {
  return field.type && !field.type.startsWith('relation') ? field.type : 'text'
}

function validationRules(field, editing, getValues) {
  const rules = {}
  const required = field.required || (field.requiredOnCreate && !editing)
  const validators = {}

  if (required && field.type !== 'relationMulti') {
    rules.required = `${field.label} este obligatoriu.`
  }

  if (required && field.type === 'relationMulti') {
    validators.requiredList = (value) => {
      const selected = Array.isArray(value) ? value : [value].filter(Boolean)
      return selected.length > 0 || `${field.label} este obligatoriu.`
    }
  }

  if (field.type === 'email') {
    rules.pattern = {
      value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
      message: 'Email invalid.',
    }
  }

  if (field.min !== undefined) {
    rules.min = {
      value: field.min,
      message: `${field.label} trebuie sa fie cel putin ${field.min}.`,
    }
  }

  if (field.minLength) {
    validators.minLength = (value) => {
      if (!value) return true
      return value.length >= field.minLength || `${field.label} trebuie sa aiba minimum ${field.minLength} caractere.`
    }
  }

  if (field.future) {
    validators.future = (value) => {
      if (!value) return true
      return new Date(value) > new Date() || `${field.label} trebuie sa fie in viitor.`
    }
  }

  if (field.afterField) {
    validators.afterField = (value) => {
      const start = getValues(field.afterField)
      if (!value || !start) return true
      return new Date(value) > new Date(start) || `${field.label} trebuie sa fie dupa ${field.afterLabel || field.afterField}.`
    }
  }

  if (Object.keys(validators).length) {
    rules.validate = validators
  }

  return rules
}

export function GenericCrudPage({ config }) {
  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [sort, setSort] = useState(`${config.sortOptions[0]},asc`)
  const [editing, setEditing] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [relationOptions, setRelationOptions] = useState({})
  const { register, handleSubmit, reset, formState, getValues } = useForm({ defaultValues: initialValues(config.fields) })

  const relationFields = useMemo(() => config.fields.filter((field) => field.source), [config.fields])
  const columns = useMemo(() => [
    { name: 'id', label: 'ID', path: 'id' },
    ...config.fields.filter((field) => field.showInTable !== false).slice(0, 5),
  ], [config.fields])

  const loadRelations = useCallback(async () => {
    const entries = await Promise.all(relationFields.map(async (field) => {
      const { data } = await http.get(field.source, { params: { page: 0, size: 100, sort: 'id,asc' } })
      return [field.name, extractItems(data)]
    }))
    setRelationOptions(Object.fromEntries(entries))
  }, [relationFields])

  const load = useCallback(async () => {
    setError('')
    if (!config.sortOptions.includes(sort.split(',')[0])) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const { data } = await http.get(config.endpoint, { params: { page, size: 8, sort } })
      setItems(extractItems(data))
      setTotalPages(data.totalPages || 1)
    } catch (exception) {
      setError(errorMessage(exception))
    } finally {
      setLoading(false)
    }
  }, [config.endpoint, config.sortOptions, page, sort])

  useEffect(() => {
    setItems([])
    setTotalPages(1)
    setSort(`${config.sortOptions[0]},asc`)
    reset(initialValues(config.fields))
    setEditing(null)
    setPage(0)
    setMessage('')
    setError('')
  }, [config, reset])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    loadRelations().catch((exception) => setError(errorMessage(exception)))
  }, [loadRelations])

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
      await Promise.all([load(), loadRelations()])
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

  function renderField(field) {
    if (field.type === 'checkbox') {
      return (
        <div key={field.name} className="span-2 form-check mt-2">
          <input className="form-check-input" type="checkbox" {...register(field.name)} />
          <label className="form-check-label">{field.label}</label>
        </div>
      )
    }

    if (field.type === 'relation' || field.type === 'relationMulti') {
      const multiple = field.type === 'relationMulti'
      return (
        <div key={field.name}>
          <label className="form-label">{field.label}</label>
          <select
            className="form-select"
            multiple={multiple}
            {...register(field.name, validationRules(field, editing, getValues))}
          >
            {!multiple && <option value="">Alege...</option>}
            {(relationOptions[field.name] || []).map((option) => (
              <option key={relationValue(option, field)} value={relationValue(option, field)}>
                {optionText(option, field)}
              </option>
            ))}
          </select>
          <div className="form-text">
            {(relationOptions[field.name] || []).length === 0 ? 'Nu exista optiuni disponibile.' : ''}
          </div>
          <div className="text-danger small">{formState.errors[field.name]?.message}</div>
        </div>
      )
    }

    return (
      <div key={field.name}>
        <label className="form-label">{field.label}</label>
        <input
          className="form-control"
          type={inputType(field)}
          min={field.min}
          step={field.step}
          placeholder={field.placeholder || ''}
          {...register(field.name, validationRules(field, editing, getValues))}
        />
        <div className="text-danger small">{formState.errors[field.name]?.message}</div>
      </div>
    )
  }

  return (
    <>
      <div className="mb-3">
        <h1 className="page-title">{config.title}</h1>
        <div className="muted">CRUD cu paginare, sortare si dropdown-uri pentru relatii.</div>
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
                  {columns.map((column) => <th key={column.name}>{column.label || column.name}</th>)}
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr><td colSpan={columns.length + 1}>Se incarca...</td></tr>
                )}
                {!loading && items.length === 0 && (
                  <tr><td colSpan={columns.length + 1}>Nu exista inregistrari.</td></tr>
                )}
                {!loading && items.map((item) => (
                  <tr key={item.id}>
                    {columns.map((column) => {
                      const field = config.fields.find((candidate) => candidate.name === column.name) || column
                      return <td key={column.name}>{display(getByPath(item, field.path || field.name), field)}</td>
                    })}
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
            {config.fields.map((field) => renderField(field))}
          </div>
          <button className="btn btn-brand mt-3" type="submit" disabled={formState.isSubmitting}>
            <Plus size={16} /> Salveaza
          </button>
        </form>
      </div>
    </>
  )
}
