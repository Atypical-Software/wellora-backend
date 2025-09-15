#!/bin/bash
echo "Iniciando build do Wellora Backend..."
mvn clean package -DskipTests
echo "Build concluído!"
