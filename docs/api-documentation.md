# API Documentation

Toate cererile frontend trec prin API Gateway: `http://localhost:8080`.

Header pentru endpoint-uri protejate:

```http
Authorization: Bearer <jwt>
```

## Auth

| Method | Path | Rol |
| --- | --- | --- |
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/logout` | public |
| GET | `/api/auth/me` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/users/**` | ADMIN |
| GET/POST/PUT/DELETE | `/api/roles/**` | ADMIN |

`POST /api/auth/login` intoarce tokenul si informatii folosite de frontend:

```json
{
  "token": "jwt",
  "type": "Bearer",
  "userId": 1,
  "username": "user",
  "email": "user@fithub.local",
  "roles": ["USER"]
}
```

## Gym

| Method | Path | Rol |
| --- | --- | --- |
| GET | `/api/classes` | USER, ADMIN |
| GET | `/api/classes/{id}` | USER, ADMIN |
| GET | `/api/classes/{id}/availability` | USER, ADMIN |
| POST | `/api/classes/{id}/reserve-slot` | USER, ADMIN |
| POST | `/api/classes/{id}/release-slot` | USER, ADMIN |
| CRUD | `/api/locations` | ADMIN |
| CRUD | `/api/rooms` | ADMIN |
| CRUD | `/api/trainers` | ADMIN |
| CRUD | `/api/class-types` | ADMIN |
| CRUD | `/api/classes` | ADMIN pentru modificari |
| CRUD | `/api/equipment` | ADMIN |

## Booking

| Method | Path | Rol |
| --- | --- | --- |
| GET | `/api/subscription-types` | USER, ADMIN |
| POST/PUT/DELETE | `/api/subscription-types/**` | ADMIN |
| GET/POST | `/api/clients/me` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/clients/**` | ADMIN |
| GET | `/api/subscriptions/me` | USER, ADMIN |
| POST | `/api/subscriptions/me/purchase` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/subscriptions/**` | ADMIN |
| GET/POST/DELETE | `/api/bookings/me/**` | USER, ADMIN |
| GET/POST/DELETE | `/api/bookings/**` | ADMIN |
| GET | `/api/payments/me` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/payments/**` | ADMIN |
| GET | `/api/notifications/me` | USER, ADMIN |
| PUT | `/api/notifications/me/{id}/read` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/notifications/**` | ADMIN |
| CRUD | `/api/clients` | ADMIN |

## Pagination And Sorting

Endpoint-urile list folosesc `Pageable`:

```http
GET /api/classes?page=0&size=8&sort=startTime,asc
GET /api/clients?page=0&size=8&sort=lastName,asc
GET /api/bookings/me?page=0&size=8&sort=bookingDate,desc
```

## Register, Subscription And Booking Flow

La register, `auth-service` creeaza utilizatorul si returneaza JWT. Frontend-ul creeaza apoi clientul asociat contului curent in `booking-service`:

```http
POST /api/clients/me
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "firstName": "User",
  "lastName": "FitHub",
  "email": "user@fithub.local",
  "phone": "0700000001"
}
```

Userul vede tipurile active de abonament:

```http
GET /api/subscription-types?page=0&size=50&sort=price,asc
Authorization: Bearer <jwt>
```

Cumpararea abonamentului demo creeaza un `client_subscription` activ si o plata cu status `PAID`:

```http
POST /api/subscriptions/me/purchase
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "subscriptionTypeId": 1,
  "paymentMethod": "CARD"
}
```

Raspuns:

```json
{
  "subscription": {
    "id": 1,
    "startDate": "2026-05-24",
    "endDate": "2026-06-23",
    "status": "ACTIVE"
  },
  "payment": {
    "id": 1,
    "amount": 149.99,
    "status": "PAID",
    "method": "CARD"
  }
}
```

Frontend-ul nu cere utilizatorului sa completeze `clientId`. La rezervare, aplicatia poate citi clientul curent:

```http
GET /api/clients/me
Authorization: Bearer <jwt>
```

Raspuns:

```json
{
  "id": 1,
  "authUserId": 2,
  "firstName": "User",
  "lastName": "FitHub",
  "email": "user@fithub.local"
}
```

Apoi trimite rezervarea scoped pe userul curent:

```http
POST /api/bookings/me
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "fitnessClassId": 1
}
```

Flux intern:

1. `booking-service` identifica userul din JWT si cauta clientul asociat.
2. `booking-service` cauta abonament activ pentru client.
3. `booking-service` apeleaza `gym-service` prin OpenFeign pentru availability.
4. `booking-service` apeleaza `reserve-slot`.
5. `booking-service` salveaza booking-ul si creeaza notificare.

Datele userului curent se citesc cu:

```http
GET /api/subscriptions/me?page=0&size=20&sort=startDate,desc
GET /api/bookings/me?page=0&size=8&sort=bookingDate,desc
GET /api/payments/me?page=0&size=10&sort=paymentDate,desc
GET /api/notifications/me?page=0&size=20&sort=id,desc
PUT /api/notifications/me/{id}/read
```

Raspunsurile de eroare au forma:

```json
{
  "status": 400,
  "message": "Clientul nu are abonament activ.",
  "timestamp": "2026-05-23T12:00:00"
}
```
