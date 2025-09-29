# Configuração das Variáveis de Ambiente no Render

## Configurações Obrigatórias para Deploy no Render

Configure as seguintes variáveis de ambiente no painel do Render:

### 1. Configurações Básicas
```
SPRING_PROFILES_ACTIVE=prod
PORT=10000
```

### 2. Banco de Dados MongoDB Atlas
```
MONGODB_URI=mongodb+srv://Wellora:Pvx5QzgPPm3PVtq5@cluster0.dfxd3lp.mongodb.net/wellora?retryWrites=true&w=majority&appName=Cluster0
```

### 3. Segurança JWT (IMPORTANTE: Gerar chaves seguras!)
```
JWT_SECRET=SuaChaveJWTSuperSeguraAqui256BitsMinimo
JWT_EXPIRATION=86400
JWT_ANONYMOUS_SECRET=SuaChaveAnonimaJWTSuperSeguraAqui256BitsMinimo
JWT_ANONYMOUS_EXPIRATION=3600
```

### 4. Configurações Opcionais
```
LOG_LEVEL=INFO
```

## ⚠️ IMPORTANTE - Segurança

1. **JWT_SECRET e JWT_ANONYMOUS_SECRET**: 
   - Use chaves aleatórias de pelo menos 256 bits (32 caracteres)
   - Nunca compartilhe essas chaves
   - Gere chaves diferentes para cada ambiente

2. **Exemplo de geração de chave segura:**
   ```bash
   # Linux/Mac
   openssl rand -base64 32
   
   # Online
   https://generate-secret.vercel.app/32
   ```

## Como Configurar no Render

1. Acesse seu serviço no painel do Render
2. Vá em **Environment**
3. Adicione cada variável listada acima
4. Clique em **Save Changes**
5. O deploy será reiniciado automaticamente

## Verificação

Após o deploy, acesse:
- `https://seu-app.onrender.com/actuator/health` - Verificar se a aplicação está rodando
- Logs do Render para verificar se não há erros de conexão com o banco