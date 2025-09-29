#!/bin/sh

echo "=== SCRIPT DE BUILD FORÇADO ==="

# Build completo primeiro
echo "1. Build completo..."
./mvnw clean package -DskipTests

echo "2. Verificando resultado..."
ls -la target/

if [ -f target/wellora-backend-1.0.0.jar ]; then
    echo "✅ JAR encontrado"
    
    # Verifica se é Spring Boot
    if jar tf target/wellora-backend-1.0.0.jar | grep -q "BOOT-INF"; then
        echo "✅ É um JAR Spring Boot executável"
    else
        echo "❌ NÃO é um JAR Spring Boot - tentando repackage..."
        ./mvnw spring-boot:repackage
        
        if jar tf target/wellora-backend-1.0.0.jar | grep -q "BOOT-INF"; then
            echo "✅ Repackage funcionou!"
        else
            echo "❌ Repackage falhou!"
            exit 1
        fi
    fi
    
    # Teste de execução
    echo "3. Testando execução..."
    java -jar target/wellora-backend-1.0.0.jar --version 2>&1 | head -3 || echo "Teste concluído"
    
else
    echo "❌ JAR NÃO encontrado!"
    exit 1
fi

echo "=== BUILD CONCLUÍDO COM SUCESSO ==="