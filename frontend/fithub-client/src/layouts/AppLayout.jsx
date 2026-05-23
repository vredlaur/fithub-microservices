import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  CalendarCheck,
  Dumbbell,
  Home,
  Layers,
  LogOut,
  MapPin,
  Receipt,
  Shield,
  Users,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth.js'

const userLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: Home },
  { to: '/classes', label: 'Clase', icon: Dumbbell },
  { to: '/bookings', label: 'Rezervari', icon: CalendarCheck },
  { to: '/subscriptions', label: 'Abonamente', icon: Receipt },
  { to: '/notifications', label: 'Notificari', icon: Bell },
]

const adminLinks = [
  { to: '/admin/users', label: 'Utilizatori', icon: Shield },
  { to: '/admin/locations', label: 'Locatii', icon: MapPin },
  { to: '/admin/rooms', label: 'Sali', icon: Layers },
  { to: '/admin/trainers', label: 'Antrenori', icon: Users },
  { to: '/admin/class-types', label: 'Tipuri clase', icon: Layers },
  { to: '/admin/classes', label: 'Clase admin', icon: Dumbbell },
  { to: '/admin/subscription-types', label: 'Tipuri abonamente', icon: Receipt },
  { to: '/admin/subscriptions', label: 'Abonamente clienti', icon: Receipt },
  { to: '/admin/payments', label: 'Plati', icon: Receipt },
  { to: '/admin/clients', label: 'Clienti', icon: Users },
]

function LinkItem({ item }) {
  const Icon = item.icon
  return (
    <NavLink className="nav-link-app" to={item.to}>
      <Icon size={17} />
      <span>{item.label}</span>
    </NavLink>
  )
}

export function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const isAdmin = user?.roles?.includes('ADMIN')

  function onLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <Dumbbell />
          FitHub
        </div>
        <div className="nav-group-title">Aplicatie</div>
        {userLinks.map((item) => (
          <LinkItem key={item.to} item={item} />
        ))}
        {isAdmin && (
          <>
            <div className="nav-group-title">Administrare</div>
            {adminLinks.map((item) => (
              <LinkItem key={item.to} item={item} />
            ))}
          </>
        )}
      </aside>
      <main className="main">
        <div className="topbar">
          <div>
            <div className="fw-semibold">{user?.username}</div>
            <div className="muted small">{user?.roles?.join(', ')}</div>
          </div>
          <button className="btn btn-outline-secondary btn-sm" type="button" onClick={onLogout}>
            <LogOut size={16} /> Logout
          </button>
        </div>
        <Outlet />
      </main>
    </div>
  )
}
