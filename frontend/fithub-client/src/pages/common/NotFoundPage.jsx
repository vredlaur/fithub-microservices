import { Home, SearchX } from 'lucide-react'
import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="panel text-center py-5">
      <SearchX size={44} className="mb-3 text-secondary" />
      <h1 className="page-title mb-2">Pagina nu a fost gasita</h1>
      <p className="muted mb-4">Adresa accesata nu exista in aplicatia FitHub.</p>
      <Link className="btn btn-brand" to="/dashboard">
        <Home size={16} /> Dashboard
      </Link>
    </section>
  )
}
