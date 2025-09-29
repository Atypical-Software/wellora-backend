# Wellora Backend API

Back-end para aplicativo de bem-estar emocional e avaliações psicossociais.

## Tecnologias

- Java 17
- Spring Boot 3.2.0
- Spring Security (JWT)
- Spring Data MongoDB
- MongoDB Atlas
- Maven

## Deploy

- Render.com
- Docker
- MongoDB Atlas

## Endpoints

### Autenticação
- POST /api/auth/login
- GET /api/auth/validate

### Check-in Emocional 
- POST /api/humor/checkin
- GET /api/humor/historico

### Questionário Psicossocial
- POST /api/questionario/responder
- GET /api/questionario/historico

### Relatórios
- GET /api/relatorio/admin
- GET /api/relatorio/usuario

### Health
- GET /api/health
- GET /api/connectivity-check  
