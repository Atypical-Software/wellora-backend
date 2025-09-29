#!/bin/bash

# Script de diagnóstico para o build
echo "=== Diagnóstico do Build ==="
echo "Data: $(date)"
echo "Diretório: $(pwd)"
echo ""

echo "=== Verificando pom.xml ==="
if grep -q "spring-boot-maven-plugin" pom.xml; then
    echo "✅ Plugin Spring Boot encontrado"
else
    echo "❌ Plugin Spring Boot NÃO encontrado"
fi

echo ""
echo "=== Conteúdo do plugin no pom.xml ==="
grep -A 10 "spring-boot-maven-plugin" pom.xml || echo "Plugin não encontrado"

echo ""
echo "=== Verificando JAR após build ==="
if [ -f "target/wellora-backend-1.0.0.jar" ]; then
    echo "✅ JAR encontrado"
    echo "Tamanho: $(ls -lh target/wellora-backend-1.0.0.jar | awk '{print $5}')"
    
    # Verifica se é executável
    if jar tf target/wellora-backend-1.0.0.jar | grep -q "BOOT-INF"; then
        echo "✅ JAR Spring Boot (executável)"
    else
        echo "❌ JAR simples (não executável)"
    fi
else
    echo "❌ JAR não encontrado"
fi

echo ""
echo "=== Manifest do JAR ==="
if [ -f "target/wellora-backend-1.0.0.jar" ]; then
    jar xf target/wellora-backend-1.0.0.jar META-INF/MANIFEST.MF
    cat META-INF/MANIFEST.MF
    rm -rf META-INF
else
    echo "JAR não encontrado para verificar manifest"
fi