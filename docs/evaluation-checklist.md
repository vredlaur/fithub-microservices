# Evaluation Checklist

Acest checklist este pentru demo si pentru verificarea cerintelor obligatorii.

## Backend

- [x] Minimum 3 microservicii business: `auth-service`, `gym-service`, `booking-service`.
- [x] API Gateway pe `localhost:8080` cu rute centralizate.
- [x] Eureka Discovery Server pe `localhost:8761`.
- [x] PostgreSQL cu baze separate: `fithub_auth_db`, `fithub_gym_db`, `fithub_booking_db`.
- [x] H2 in-memory pentru profilul `test`.
- [x] Spring Security cu JWT, BCrypt si roluri `ADMIN`/`USER`.
- [x] CRUD pentru entitatile principale.
- [x] Bean Validation pe campurile importante.
- [x] Global exception handling cu raspuns standard.
- [x] Logging in fisiere pe fiecare serviciu.
- [x] OpenFeign din `booking-service` catre `gym-service`.
- [x] Actuator health.
- [x] Swagger/OpenAPI.

## Frontend

- [x] React Vite cu rute protejate.
- [x] Login, register, logout.
- [x] Meniu diferit pentru `ADMIN` si `USER`.
- [x] Dashboard, clase, rezervari, abonamente, notificari.
- [x] Paginile admin pentru utilizatori, locatii, sali, traineri, tipuri clase, clase, tipuri abonamente, clienti, abonamente si plati.
- [x] Formulare cu validare client-side.
- [x] Dropdown-uri pentru relatii in loc de ID-uri brute.
- [x] Rezervare user fara completare manuala `clientId`.
- [x] Pagina 404.

## Testing

- [x] Unit tests JUnit 5 + Mockito pe service layer.
- [x] Integration test: register + login user.
- [x] Integration test: admin creeaza date de gym si clasa.
- [x] Integration test: client cu abonament activ face rezervare.
- [x] JaCoCo check minimum 70% pe service layer.

## Demo Commands

```powershell
.\mvnw.cmd test

cd frontend\fithub-client
npm ci
npm run lint
npm run build

cd ..\..
docker compose up --build
```

## Dovezi In Demo

- Eureka arata `AUTH-SERVICE`, `GYM-SERVICE`, `BOOKING-SERVICE`, `API-GATEWAY`.
- Swagger se deschide pentru cele 3 servicii business.
- In pgAdmin se vad tabelele generate in cele 3 baze.
- Rezervarea creeaza rand in `bookings` si notificare in `notifications`.
- Sloturile clasei scad in `fitness_classes.available_slots`.
