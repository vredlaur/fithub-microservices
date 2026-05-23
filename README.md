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

Rapoartele JaCoCo se genereaza in:

- `backend/auth-service/target/site/jacoco/index.html`
- `backend/gym-service/target/site/jacoco/index.html`
- `backend/booking-service/target/site/jacoco/index.html`

Build-ul verifica minimum 70% coverage pe clasele din pachetele `service`.

Frontend:

```powershell
cd frontend\fithub-client
npm ci
npm run lint
npm run build
npm run dev
```

Docker Compose:

```powershell
docker compose up --build
```

Demo rapid in 5 minute:

```powershell
.\scripts\demo-reset.ps1
```

Comanda de mai sus sterge volumul PostgreSQL, reconstruieste containerele, porneste aplicatia si ruleaza verificarea automata. Pentru verificare fara reset:

```powershell
.\scripts\demo-check.ps1
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

Pentru un demo curat, ruleaza `.\scripts\demo-reset.ps1`. Seed data creeaza conturile `admin` si `user`, o locatie, o sala, un trainer, o clasa fitness, un client legat de userul demo, un abonament activ si o plata.

## Flux Demo Principal

1. Login cu `admin`.
2. Admin verifica sau creeaza locatie, sala, antrenor, tip clasa si clasa fitness folosind dropdown-urile din paginile CRUD.
3. Admin verifica pagina `Utilizatori`, unde profilul afiseaza nume, prenume si telefon.
4. Login cu `user` sau register cu un user nou.
5. User cumpara abonament din `/subscriptions`; frontend-ul foloseste `/api/subscriptions/me/purchase`.
6. User vede clasele disponibile.
7. User face rezervare fara sa completeze manual `clientId`; frontend-ul foloseste `/api/bookings/me`.
8. `booking-service` verifica abonamentul activ si cere catre `gym-service` rezervarea unui slot.
9. Se creeaza rezervarea, plata si notificarea.
10. In Eureka apar `AUTH-SERVICE`, `GYM-SERVICE`, `BOOKING-SERVICE`, `API-GATEWAY`.

Verificare automata:

```powershell
.\scripts\demo-check.ps1
```

Scriptul verifica health checks, login, Eureka, `/api/clients/me`, lista de clase si fluxul de rezervare prin API Gateway folosind endpoint-ul `/api/bookings/me`.

## Checklist Cerinte

- Microservicii reale: `auth-service`, `gym-service`, `booking-service`.
- Componente Spring Cloud: Eureka Discovery Server si API Gateway.
- Securitate: JWT, BCrypt, roluri `ADMIN` si `USER`, endpoint-uri protejate.
- Persistenta: PostgreSQL cu baze separate pentru fiecare microserviciu business; H2 pentru teste.
- CRUD, validari, exception handling si logging in serviciile business.
- Paginare si sortare pentru clase, clienti si rezervari/plati.
- Comunicare intre microservicii prin OpenFeign si load balancing prin Eureka.
- Actuator health si Swagger/OpenAPI pe serviciile backend.
- Frontend React cu login/register, rute protejate, meniu pe roluri, CRUD admin, rezervari si notificari.
- Teste unitare pe service layer, 3 scenarii integration si JaCoCo cu prag minim de 70% pe service layer.
- Docker Compose porneste PostgreSQL, Eureka, gateway-ul, serviciile business si frontend-ul.

## Documentatie

- [Arhitectura](docs/architecture.md)
- [ER Diagram](docs/er-diagram.md)
- [API Documentation](docs/api-documentation.md)
- [Checklist evaluare](docs/evaluation-checklist.md)
- [Script demo](docs/demo-script.md)
- Screenshots:
  - `docs/screenshots/frontend-dashboard.png`
  - `docs/screenshots/admin-classes-dropdowns.png`
  - `docs/screenshots/admin-users-profile.png`
  - `docs/screenshots/subscriptions-purchase.png`
  - `docs/screenshots/bookings-confirmed.png`
  - `docs/screenshots/frontend-404.png`
  - `docs/screenshots/eureka-services.png`
  - `docs/screenshots/swagger-auth.png`
  - `docs/screenshots/swagger-booking.png`

## Branch Strategy

- `main`: versiuni stabile.
- `dev`: integrare curenta.
- feature branches pentru functionalitati mari.

## Limitari Cunoscute

- Deployment-ul este local prin Docker Compose, nu cloud public.
- Config Server, Redis, Prometheus/Grafana si Resilience4j sunt lasate ca extensii.
- JWT foloseste un secret local configurabil prin `JWT_SECRET`; pentru productie trebuie schimbat si externalizat.
