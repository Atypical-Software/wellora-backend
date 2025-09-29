# Wellora Backend API

Back-end para aplicativo de bem-estar emocional e avaliações psicossociais conforme NR-1.

## Funcionalidades Implementadas

### 1. Autenticação JWT
- POST /api/auth/login - Login de usuários
- GET /api/auth/validate - Validação de token

### 2. Check-in Emocional 
- POST /api/humor/checkin - Registro diário de humor
- GET /api/humor/historico - Histórico de humor do usuário

### 3. Questionário Psicossocial
- POST /api/questionario/responder - Submeter questionário
- GET /api/questionario/historico - Histórico de questionários

### 4. Relatórios Administrativos
- GET /api/relatorio/admin - Dashboard para RH
- GET /api/relatorio/usuario - Relatório individual

## Tecnologias

- Java 17
- Spring Boot 3.2.0
- Spring Security (JWT)
- Spring Data MongoDB
- MongoDB
- Maven

## Como Executar

1. Instale MongoDB
2. Configure application.yml
3. Execute: mvn spring-boot:run

## Conformidade NR-1

- Logs de auditoria para todas as ações
- Registro de avaliações psicossociais
- Histórico completo de dados
- Relatórios para acompanhamento contínuo
"# Deploy fix - JAR executavel"  
