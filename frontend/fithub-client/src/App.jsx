import { Navigate, Route, Routes } from 'react-router-dom'
import { Dumbbell, LayoutDashboard } from 'lucide-react'
import { useAuth } from './hooks/useAuth.js'
import { AppLayout } from './layouts/AppLayout.jsx'
import { LoginPage } from './pages/auth/LoginPage.jsx'
import { RegisterPage } from './pages/auth/RegisterPage.jsx'
import { DashboardPage } from './pages/dashboard/DashboardPage.jsx'
import { ClassesPage } from './pages/classes/ClassesPage.jsx'
import { ClassDetailsPage } from './pages/classes/ClassDetailsPage.jsx'
import { BookingsPage } from './pages/bookings/BookingsPage.jsx'
import { SimplePage } from './pages/common/SimplePage.jsx'
import { NotFoundPage } from './pages/common/NotFoundPage.jsx'
import { GenericCrudPage } from './pages/admin/GenericCrudPage.jsx'
import { adminResources } from './services/resources.js'

function ProtectedRoute({ children, roles }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (roles?.length && !roles.some((role) => user.roles.includes(role))) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}

function AdminPage({ resourceKey }) {
  return (
    <ProtectedRoute roles={['ADMIN']}>
      <GenericCrudPage config={adminResources[resourceKey]} />
    </ProtectedRoute>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="classes" element={<ClassesPage />} />
        <Route path="classes/:id" element={<ClassDetailsPage />} />
        <Route path="bookings" element={<BookingsPage />} />
        <Route path="subscriptions" element={<SimplePage title="Abonamente" endpoint="/subscriptions" icon={Dumbbell} />} />
        <Route path="notifications" element={<SimplePage title="Notificari" endpoint="/notifications" markRead icon={LayoutDashboard} />} />
        {Object.keys(adminResources).map((key) => (
          <Route key={key} path={`admin/${key}`} element={<AdminPage resourceKey={key} />} />
        ))}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
