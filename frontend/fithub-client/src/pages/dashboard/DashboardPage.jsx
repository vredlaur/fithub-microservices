import { Activity, CalendarCheck, Dumbbell, Server } from 'lucide-react'
import { useEffect, useState } from 'react'

export function DashboardPage() {
  const [health, setHealth] = useState('necunoscut')

  useEffect(() => {
    fetch('http://localhost:8080/actuator/health')
      .then((response) => response.json())
      .then((data) => setHealth(data.status))
      .catch(() => setHealth('offline'))
  }, [])

  return (
    <>
      <div className="mb-4">
        <h1 className="page-title">Dashboard</h1>
        <div className="muted">Conturi demo: admin/Admin123! si user/User123!</div>
      </div>
      <div className="metric-grid">
        <div className="metric">
          <Dumbbell size={22} />
          <strong>3</strong>
          <span className="muted">microservicii business</span>
        </div>
        <div className="metric">
          <Server size={22} />
          <strong>{health}</strong>
          <span className="muted">gateway health</span>
        </div>
        <div className="metric">
          <CalendarCheck size={22} />
          <strong>Feign</strong>
          <span className="muted">booking catre gym</span>
        </div>
        <div className="metric">
          <Activity size={22} />
          <strong>Eureka</strong>
          <span className="muted">service discovery</span>
        </div>
      </div>
      <div className="panel mt-3">
        <h2 className="h5 fw-bold">Flux demo principal</h2>
        <ol className="mb-0">
          <li>Admin creeaza locatii, sali, antrenori si clase.</li>
          <li>User vede clasele disponibile.</li>
          <li>Booking-service verifica abonamentul si rezerva slot in gym-service.</li>
        </ol>
      </div>
    </>
  )
}
