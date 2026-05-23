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
| GET/POST/DELETE | `/api/bookings` | USER, ADMIN |
| GET/POST/PUT/DELETE | `/api/subscriptions` | USER, ADMIN |
| GET/POST | `/api/payments` | USER, ADMIN |
| GET/PUT | `/api/notifications` | USER, ADMIN |
| GET | `/api/clients/me` | USER, ADMIN |
| CRUD | `/api/clients` | ADMIN |
| CRUD | `/api/subscription-types` | ADMIN |

## Pagination And Sorting

Endpoint-urile list folosesc `Pageable`:

```http
GET /api/classes?page=0&size=8&sort=startTime,asc
GET /api/clients?page=0&size=8&sort=lastName,asc
GET /api/bookings?page=0&size=8&sort=bookingDate,desc
```

## Booking Flow Request

Frontend-ul nu cere utilizatorului sa completeze `clientId`. La rezervare, aplicatia cere mai intai:

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

Apoi trimite rezervarea catre `booking-service`:

```http
POST /api/bookings
Content-Type: application/json

{
  "clientId": 1,
  "fitnessClassId": 1
}
```

Flux intern:

1. `booking-service` verifica daca exista clientul.
2. `booking-service` cauta abonament activ pentru client.
3. `booking-service` apeleaza `gym-service` prin OpenFeign pentru availability.
4. `booking-service` apeleaza `reserve-slot`.
5. `booking-service` salveaza booking-ul si creeaza notificare.

Raspunsurile de eroare au forma:

```json
{
  "status": 400,
  "message": "Clientul nu are abonament activ.",
  "timestamp": "2026-05-23T12:00:00"
}
```
