import { CheckCircle2, CreditCard, Receipt } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage, http } from '../../api/http.js'

function items(data) {
  return data?.content || data || []
}

function formatDate(value) {
  return value ? value.slice(0, 10) : '-'
}

function price(value) {
  if (value === null || value === undefined) return '-'
  return `${Number(value).toFixed(2)} lei`
}

export function SubscriptionsPage() {
  const [plans, setPlans] = useState([])
  const [subscriptions, setSubscriptions] = useState([])
  const [payments, setPayments] = useState([])
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const [loading, setLoading] = useState(true)
  const [buyingId, setBuyingId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const activeSubscription = useMemo(
    () => subscriptions.find((item) => item.status === 'ACTIVE' && item.endDate >= new Date().toISOString().slice(0, 10)),
    [subscriptions],
  )

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [plansResponse, subscriptionsResponse, paymentsResponse] = await Promise.all([
        http.get('/subscription-types', { params: { page: 0, size: 50, sort: 'price,asc' } }),
        http.get('/subscriptions/me', { params: { page: 0, size: 20, sort: 'startDate,desc' } }),
        http.get('/payments/me', { params: { page: 0, size: 10, sort: 'paymentDate,desc' } }),
      ])
      setPlans(items(plansResponse.data).filter((plan) => plan.active))
      setSubscriptions(items(subscriptionsResponse.data))
      setPayments(items(paymentsResponse.data))
    } catch (exception) {
      setError(errorMessage(exception))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function purchase(plan) {
    setError('')
    setMessage('')
    setBuyingId(plan.id)
    try {
      await http.post('/subscriptions/me/purchase', {
        subscriptionTypeId: plan.id,
        paymentMethod,
      })
      setMessage(`Abonamentul ${plan.name} a fost activat.`)
      await load()
    } catch (exception) {
      setError(errorMessage(exception))
    } finally {
      setBuyingId(null)
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-end gap-3 flex-wrap mb-3">
        <div>
          <h1 className="page-title">Abonamente</h1>
          <div className="muted">Alege un abonament activ pentru a putea face rezervari.</div>
        </div>
        <select className="form-select w-auto" value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>
          <option value="CARD">Card</option>
          <option value="CASH">Cash</option>
        </select>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row g-3">
        <div className="col-12 col-xl-7">
          <section className="panel h-100">
            <h2 className="h5 fw-bold mb-3">Tipuri disponibile</h2>
            {loading && <div>Se incarca...</div>}
            {!loading && plans.length === 0 && <div className="muted">Nu exista tipuri de abonament active.</div>}
            <div className="row g-3">
              {plans.map((plan) => (
                <div className="col-12 col-md-6" key={plan.id}>
                  <article className="border rounded p-3 h-100">
                    <div className="d-flex justify-content-between gap-2 mb-2">
                      <h3 className="h6 fw-bold mb-0">{plan.name}</h3>
                      <span className="badge text-bg-success">{plan.durationDays} zile</span>
                    </div>
                    <div className="muted small mb-2">{plan.description || 'Acces FitHub'}</div>
                    <div className="fs-5 fw-bold mb-3">{price(plan.price)}</div>
                    <button
                      className="btn btn-brand btn-sm"
                      type="button"
                      onClick={() => purchase(plan)}
                      disabled={buyingId === plan.id}
                    >
                      <CreditCard size={15} /> Cumpara
                    </button>
                  </article>
                </div>
              ))}
            </div>
          </section>
        </div>

        <div className="col-12 col-xl-5">
          <section className="panel mb-3">
            <h2 className="h5 fw-bold mb-3">Abonamentul tau</h2>
            {activeSubscription ? (
              <div className="d-flex align-items-start gap-2">
                <CheckCircle2 className="text-success mt-1" size={20} />
                <div>
                  <div className="fw-semibold">{activeSubscription.subscriptionType?.name}</div>
                  <div className="muted small">
                    Activ pana la {formatDate(activeSubscription.endDate)}
                  </div>
                  <Link className="btn btn-outline-success btn-sm mt-3" to="/classes">
                    Vezi clasele
                  </Link>
                </div>
              </div>
            ) : (
              <div className="muted">Nu ai inca un abonament activ.</div>
            )}
          </section>

          <section className="panel">
            <h2 className="h5 fw-bold mb-3">Istoric plati</h2>
            {payments.length === 0 && <div className="muted">Nu exista plati.</div>}
            {payments.map((payment) => (
              <div className="d-flex justify-content-between gap-2 border-bottom py-2" key={payment.id}>
                <div>
                  <div className="fw-semibold">
                    <Receipt size={15} /> {payment.clientSubscription?.subscriptionType?.name || `Plata #${payment.id}`}
                  </div>
                  <div className="muted small">{formatDate(payment.paymentDate)} - {payment.method}</div>
                </div>
                <div className="fw-semibold">{price(payment.amount)}</div>
              </div>
            ))}
          </section>
        </div>
      </div>
    </>
  )
}
