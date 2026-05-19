# fithub-microservices
# FitHub - Aplicație Web cu Arhitectură de Microservicii

## Descriere proiect

FitHub este o platformă web pentru administrarea unei săli de fitness. Aplicația permite gestionarea utilizatorilor, rolurilor, clienților, antrenorilor, locațiilor, sălilor de antrenament, claselor fitness, abonamentelor, rezervărilor și notificărilor.

Proiectul este dezvoltat pentru disciplina Aplicații Web cu Arhitectură de Microservicii.

## Tema aleasă

Platformă web cu microservicii pentru administrarea unei săli de fitness.

## Tehnologii propuse

### Backend

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Cloud
- JUnit 5
- Mockito
- SLF4J + Logback

### Frontend

- React
- React Router
- Axios
- React Hook Form

### Bază de date

- PostgreSQL pentru mediul de dezvoltare
- H2 in-memory pentru mediul de testare

### Microservicii și infrastructură

- Spring Cloud Config
- Eureka Discovery Server
- Spring Cloud Gateway
- Spring Boot Actuator
- Resilience4j
- Redis
- Docker
- Docker Compose
- Prometheus
- Grafana

## Funcționalități principale

- Autentificare și autorizare pe roluri
- Administrare utilizatori
- Administrare roluri
- Administrare clienți
- Administrare antrenori
- Administrare locații
- Administrare săli de antrenament
- Administrare clase fitness
- Gestionare abonamente
- Rezervări la clase
- Notificări
- Paginare și sortare
- Validare server-side și client-side
- Logging
- Testare automată
- Deployment cu Docker

## Roluri aplicație

- ADMIN - gestionează întreaga aplicație
- TRAINER - gestionează clasele proprii
- USER - cumpără abonamente și face rezervări la clase

## Model de date propus

Entități principale:

- Utilizator
- Rol
- ProfilClient
- Antrenor
- Locatie
- SalaAntrenament
- TipClasa
- ClasaFitness
- TipAbonament
- AbonamentClient
- RezervareClasa
- Notificare

## Relații propuse

- One-to-One: Utilizator - ProfilClient
- One-to-Many: Locatie - SalaAntrenament
- One-to-Many: SalaAntrenament - ClasaFitness
- One-to-Many: TipAbonament - AbonamentClient
- Many-to-Many: Utilizator - Rol
- Many-to-Many: Client - ClasaFitness prin RezervareClasa

## Arhitectură propusă

Aplicația va fi dezvoltată inițial ca aplicație modulară, apoi va fi separată în microservicii independente.

Microservicii propuse:

- auth-service
- gym-service
- subscription-service
- booking-service
- notification-service
- api-gateway
- config-server
- discovery-server

## Branch strategy

- main - versiunea stabilă
- dev - dezvoltare activă
- feature/* - funcționalități noi

## Setup local

Instrucțiunile de instalare și rulare vor fi completate după generarea structurii inițiale a proiectului.

## Membrii echipei

- Laurențiu Vrednicu
