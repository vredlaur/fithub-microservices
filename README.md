# FitHub Microservices

FitHub este o aplicatie web pentru administrarea unei sali fitness: utilizatori, roluri, locatii, sali, antrenori, clase, abonamente, rezervari, plati si notificari. Proiectul este construit ca arhitectura de microservicii cu React, Spring Boot, Eureka, API Gateway si PostgreSQL.

## Arhitectura

```text
React Vite
  -> API Gateway :8080
    -> auth-service :8081
    -> gym-service :8082
    -> booking-service :8083
  -> discovery-server / Eureka :8761
PostgreSQL :5432
```

Microservicii business:
- `auth-service`: autentificare, JWT, roluri, utilizatori.
- `gym-service`: locatii, sali, antrenori, tipuri de clase, clase fitness, echipamente.
- `booking-service`: clienti, abonamente, rezervari, plati, notificari.

Componente tehnice:
- `api-gateway`: routing centralizat, JWT filter, rate limiting simplu.
- `discovery-server`: Eureka service registry.
- `frontend/fithub-client`: React + Vite.

## Rulare Locala

Prerequisites:
- Java 21
- Node.js + NPM
- Docker Desktop pentru rularea completa cu Compose

Backend tests:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend\fithub-client
npm install
npm run dev
```

Docker Compose:

```powershell
docker compose up --build
```

URL-uri:
- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Auth Swagger: http://localhost:8081/swagger-ui.html
- Gym Swagger: http://localhost:8082/swagger-ui.html
- Booking Swagger: http://localhost:8083/swagger-ui.html

Conturi demo:
- `admin` / `Admin123!`
- `user` / `User123!`

## Baze De Date

Un singur container PostgreSQL contine baze separate:
- `fithub_auth_db`
- `fithub_gym_db`
- `fithub_booking_db`

Pentru teste, serviciile folosesc H2 in-memory prin profilul `test`.

## Flux Demo Principal

1. Login cu `admin`.
2. Admin verifica sau creeaza locatie, sala, antrenor, tip clasa si clasa fitness.
3. Login cu `user`.
4. User vede clasele disponibile.
5. User face rezervare.
6. `booking-service` verifica abonamentul activ si cere catre `gym-service` rezervarea unui slot.
7. Se creeaza rezervarea si notificarea.
8. In Eureka apar `AUTH-SERVICE`, `GYM-SERVICE`, `BOOKING-SERVICE`, `API-GATEWAY`.

## Documentatie

- [Arhitectura](docs/architecture.md)
- [ER Diagram](docs/er-diagram.md)
- [API Documentation](docs/api-documentation.md)
- Screenshots: `docs/screenshots/`

## Branch Strategy

- `main`: versiuni stabile.
- `dev`: integrare curenta.
- feature branches pentru functionalitati mari.

## Limitari Cunoscute

- Deployment-ul este local prin Docker Compose, nu cloud public.
- Config Server, Redis, Prometheus/Grafana si Resilience4j sunt lasate ca extensii.
- JWT foloseste un secret local configurabil prin `JWT_SECRET`; pentru productie trebuie schimbat si externalizat.
