# Demo Script

## 1. Pornire

```powershell
docker compose up --build
```

Deschide:
- Frontend: `http://localhost:5173`
- Eureka: `http://localhost:8761`
- Gateway health: `http://localhost:8080/actuator/health`

## 2. Login Admin

1. Intra pe frontend.
2. Login cu `admin` / `Admin123!`.
3. Arata meniul admin.
4. Deschide `Admin > Clase`.
5. Creeaza sau editeaza o clasa folosind dropdown-uri pentru tip clasa, antrenor si sala.

## 3. Login User Si Rezervare

1. Logout.
2. Login cu `user` / `User123!`.
3. Deschide `Clase`.
4. Alege o clasa si apasa rezervare.
5. Explica faptul ca frontend-ul cere automat `/api/clients/me`, deci userul nu introduce `clientId`.
6. Deschide `Rezervari` si `Notificari`.

## 4. Verificare In pgAdmin

In `fithub_booking_db` verifica:
- `bookings`
- `notifications`
- `client_subscriptions`

In `fithub_gym_db` verifica:
- `fitness_classes.available_slots`

## 5. Cerinte Tehnice

1. Arata Eureka cu serviciile inregistrate.
2. Arata Swagger pentru auth, gym si booking.
3. Ruleaza sau arata rezultatul:

```powershell
.\mvnw.cmd test
```

4. Mentioneaza ca JaCoCo impune minimum 70% coverage pe service layer.
