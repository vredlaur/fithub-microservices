# Presentation Script

Scop: prezentare rapida, clara si repetabila pentru examen. Durata tinta: 5-7 minute.

## 1. Introducere

FitHub este o aplicatie web pentru administrarea unei sali fitness. Aplicatia permite autentificare pe roluri, administrarea datelor de sala si fluxul de user pentru cumparare abonament si rezervare la clase.

Arhitectura este bazata pe microservicii:
- React Vite pentru frontend;
- API Gateway ca punct unic de intrare;
- `auth-service` pentru useri, roluri si JWT;
- `gym-service` pentru locatii, sali, antrenori si clase;
- `booking-service` pentru clienti, abonamente, plati, rezervari si notificari;
- Eureka Discovery Server pentru service discovery;
- PostgreSQL cu baze separate pentru servicii.

## 2. Pornire Si Verificare

Arat comanda:

```powershell
.\scripts\demo-reset.ps1
```

Aceasta recreeaza mediul Docker, reseteaza volumul PostgreSQL, porneste serviciile si ruleaza verificarea automata.

Apoi arat:

```powershell
.\scripts\demo-check.ps1
```

Verificarea confirma health checks, login admin/user, Eureka, clasele disponibile si booking-ul demo prin API Gateway.

## 3. Service Discovery Si Gateway

Deschid Eureka:

```text
http://localhost:8761
```

Explic ca aici apar serviciile:
- `API-GATEWAY`;
- `AUTH-SERVICE`;
- `GYM-SERVICE`;
- `BOOKING-SERVICE`.

Deschid health pentru gateway:

```text
http://localhost:8080/actuator/health
```

Explic ca frontend-ul nu apeleaza direct microserviciile, ci foloseste `http://localhost:8080/api/...`.

## 4. Swagger Si API

Deschid Swagger pentru servicii:

```text
http://localhost:8081/swagger-ui/index.html
http://localhost:8082/swagger-ui/index.html
http://localhost:8083/swagger-ui/index.html
```

Mentionez endpoint-urile importante:
- `/api/auth/login`;
- `/api/clients/me`;
- `/api/subscriptions/me/purchase`;
- `/api/bookings/me`;
- `/api/classes/{id}/availability`.

## 5. Flux Admin

In frontend:

```text
http://localhost:5173
```

Login:

```text
admin / Admin123!
```

Arat:
- meniul de administrare;
- pagina `Utilizatori`, unde se vad nume, prenume si telefon;
- pagina `Clase admin`, unde formularul foloseste dropdown-uri pentru tip clasa, antrenor si sala;
- paginile `Abonamente clienti` si `Plati`, cu actiuni uniforme de editare/stergere.

Explic ca endpoint-urile admin sunt protejate cu rol `ADMIN`.

## 6. Flux User

Logout, apoi login:

```text
user / User123!
```

Sau creez un user nou din `Register`.

Arat:
- `Abonamente`: userul cumpara un abonament;
- `Clase`: userul alege o clasa;
- `Detalii clasa`: userul rezerva fara sa introduca `clientId`;
- `Rezervari`: rezervarea apare cu status `CONFIRMED`;
- `Notificari`: apare notificarea de rezervare;
- `Abonamente`: se vede plata demo.

Explic fluxul:

```text
React -> API Gateway -> booking-service -> gym-service
```

`booking-service` verifica abonamentul activ, cere disponibilitatea clasei de la `gym-service` prin Feign, salveaza rezervarea, creeaza plata/notificarea si scade sloturile disponibile.

## 7. Verificare In pgAdmin

In `fithub_booking_db` arat:

```sql
select * from clients order by id desc;
select * from client_subscriptions order by id desc;
select * from payments order by id desc;
select * from bookings order by id desc;
select * from notifications order by id desc;
```

In `fithub_gym_db` arat:

```sql
select id, name, capacity, available_slots, status from fitness_classes;
```

## 8. Teste Si Coverage

Rulez sau mentionez:

```powershell
.\mvnw.cmd test
```

Explic ca testele folosesc JUnit 5, Mockito, H2 pentru profilul `test` si JaCoCo pentru coverage pe service layer.

## 9. Concluzie

Proiectul acopera cerintele principale:
- autentificare si autorizare cu roluri;
- CRUD cu validari;
- paginare si sortare;
- profile dev/test;
- PostgreSQL si H2;
- logging si exception handling;
- microservicii cu Eureka, Gateway si Feign;
- Docker Compose;
- Swagger, Actuator, teste si documentatie.
