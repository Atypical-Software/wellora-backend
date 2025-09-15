# Instruções de Instalação e Configuração

## Pré-requisitos
1. Java 17 ou superior
2. Maven 3.6 ou superior  
3. MongoDB 4.4 ou superior

## Configuração do MongoDB

### Instalação Local
1. Baixe MongoDB Community Server
2. Instale seguindo as instruções do SO
3. Inicie o serviço MongoDB
4. O banco 'wellora' será criado automaticamente

### MongoDB Atlas (Nuvem)
1. Crie conta no MongoDB Atlas
2. Crie um cluster gratuito
3. Configure usuário e senha
4. Atualize a URI no application.yml

## Configuração da Aplicação

### application.yml
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/wellora
      # Para Atlas: mongodb+srv://user:pass@cluster.mongodb.net/wellora

jwt:
  secret: sua-chave-secreta-aqui
  expiration: 86400 # 24 horas
```

## Executando a Aplicação

### Desenvolvimento
```bash
mvn spring-boot:run
```

### Produção
```bash
mvn clean package
java -jar target/wellora-backend-1.0.0.jar
```

## Testando a API

### Swagger UI
Acesse: http://localhost:8080/swagger-ui.html

### Endpoints Principais
- POST /api/auth/login
- POST /api/humor/checkin  
- POST /api/questionario/responder
- GET /api/relatorio/admin

## Dados Iniciais

Crie usuários manualmente no MongoDB ou implemente DataLoader.

### Exemplo de Usuário Admin
```json
{
  "email": "admin@wellora.com",
  "senha": "$2a$10$...", // senha criptografada
  "nome": "Administrador",
  "roles": ["ADMIN"],
  "ativo": true
}
```
