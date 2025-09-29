#!/bin/bash

# Script para gerar chaves JWT seguras
# Execute este script para gerar chaves aleatórias para JWT

echo "=== Gerador de Chaves JWT Seguras ==="
echo ""

# Verifica se openssl está disponível
if command -v openssl &> /dev/null; then
    echo "🔑 Chave JWT Admin:"
    openssl rand -base64 32
    echo ""
    echo "🔑 Chave JWT Anonymous:"
    openssl rand -base64 32
    echo ""
else
    echo "⚠️  OpenSSL não encontrado. Use uma das alternativas:"
    echo ""
    echo "1. Online: https://generate-secret.vercel.app/32"
    echo "2. Node.js: node -e \"console.log(require('crypto').randomBytes(32).toString('base64'))\""
    echo "3. Python: python -c \"import secrets; print(secrets.token_urlsafe(32))\""
    echo ""
fi

echo "📋 Instruções:"
echo "1. Copie as chaves geradas"
echo "2. Configure no Render em Environment Variables:"
echo "   - JWT_SECRET=<primeira_chave>"
echo "   - JWT_ANONYMOUS_SECRET=<segunda_chave>"
echo ""
echo "⚠️  IMPORTANTE: Nunca compartilhe essas chaves!"