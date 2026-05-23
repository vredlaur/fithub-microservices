# Demo Script

## 1. Pornire

Pentru demo curat, foloseste comanda completa:

```powershell
.\scripts\demo-reset.ps1
```

Aceasta comanda executa `docker compose down -v`, reconstruieste containerele, porneste aplicatia si ruleaza `.\scripts\demo-check.ps1`.

Pentru pornire fara reset:

```powershell
docker compose up -d --build
.\scripts\demo-check.ps1
```

Deschide:
- Frontend: `http://localhost:5173`
- Eureka: `http://localhost:8761`
- Gateway health: `http://localhost:8080/actuator/health`

## 2. Login Admin

1. Intra pe frontend.
2. Login cu `admin` / `Admin123!`.
3. Arata meniul admin.
4. Deschide `Admin > Utilizatori` si arata ca numele, prenumele si telefonul sunt afisate corect.
5. Deschide `Admin > Clase`.
6. Creeaza sau editeaza o clasa folosind dropdown-uri pentru tip clasa, antrenor si sala.
7. Arata ca formularele valideaza campuri obligatorii, email, numere pozitive si intervale de date.

## 3. Login/Register User, Abonament Si Rezervare

1. Logout.
2. Login cu `user` / `User123!` sau creeaza un cont nou prin `Register`.
3. Daca folosesti cont nou, deschide `Abonamente` si apasa `Cumpara`.
4. Explica faptul ca frontend-ul creeaza clientul curent cu `/api/clients/me` si cumpara abonamentul cu `/api/subscriptions/me/purchase`.
5. Deschide `Clase`.
6. Alege o clasa si apasa rezervare.
7. Explica faptul ca frontend-ul trimite doar `fitnessClassId` catre `/api/bookings/me`; userul nu introduce `clientId`.
8. Deschide `Rezervari`, `Abonamente` si `Notificari`.
9. Explica faptul ca `booking-service` verifica abonamentul activ si apoi apeleaza `gym-service` prin Feign.

## 4. Verificare In pgAdmin

In `fithub_booking_db` verifica:
- `clients`
- `client_subscriptions`
- `payments`
- `bookings`
- `notifications`

In `fithub_gym_db` verifica:
- `fitness_classes.available_slots`

## 5. Cerinte Tehnice

1. Arata Eureka cu serviciile inregistrate.
2. Arata Swagger pentru auth, gym si booking.
3. Ruleaza sau arata rezultatul verificarii automate:

```powershell
.\scripts\demo-check.ps1
```

4. Ruleaza sau arata rezultatul testelor:

```powershell
.\mvnw.cmd test
```

5. Mentioneaza ca JaCoCo impune minimum 70% coverage pe service layer.

## 6. Reset Daca Demo-ul A Fost Rulat De Mai Multe Ori

Daca sloturile clasei s-au consumat sau datele au fost modificate manual, ruleaza:

```powershell
.\scripts\demo-reset.ps1
```

Aceasta comanda sterge volumul PostgreSQL si recreeaza datele demo prin initializatoarele Spring Boot.
