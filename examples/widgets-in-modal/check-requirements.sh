#!/bin/bash

# Script para verificar que el sistema tiene todos los requisitos necesarios
# para ejecutar los tests de Android en Docker

echo "🔍 Verificando requisitos del sistema para tests de Android en Docker"
echo "====================================================================="
echo ""

ERRORS=0
WARNINGS=0

# Check Docker
echo "📦 Verificando Docker..."
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo "   ✅ Docker instalado: $DOCKER_VERSION"

    # Check if Docker daemon is running
    if docker ps &> /dev/null; then
        echo "   ✅ Docker daemon corriendo"
    else
        echo "   ❌ Docker daemon NO está corriendo"
        echo "      Inicia Docker Desktop o ejecuta: sudo systemctl start docker"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "   ❌ Docker NO está instalado"
    echo "      Instala Docker desde: https://docs.docker.com/get-docker/"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Docker Compose
echo "🐳 Verificando Docker Compose..."
if docker compose version &> /dev/null; then
    COMPOSE_VERSION=$(docker compose version)
    echo "   ✅ Docker Compose instalado: $COMPOSE_VERSION"
else
    echo "   ❌ Docker Compose NO está instalado"
    echo "      Instala Docker Compose o actualiza Docker Desktop"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check KVM (Linux only)
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "🚀 Verificando KVM (aceleración de hardware)..."
    if [ -e /dev/kvm ]; then
        echo "   ✅ KVM disponible: /dev/kvm"

        # Check permissions
        if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
            echo "   ✅ Permisos correctos para /dev/kvm"
        else
            echo "   ⚠️  Permisos insuficientes para /dev/kvm"
            echo "      Ejecuta: sudo chmod 666 /dev/kvm"
            echo "      O agrega tu usuario al grupo kvm: sudo usermod -aG kvm $USER"
            WARNINGS=$((WARNINGS + 1))
        fi
    else
        echo "   ❌ KVM NO disponible"
        echo "      Instala: sudo apt-get install qemu-kvm"
        ERRORS=$((ERRORS + 1))
    fi
    echo ""
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 Sistema macOS detectado"
    echo "   ℹ️  Docker Desktop en macOS usa virtualización nativa"
    echo "   ℹ️  No se requiere KVM"
    echo ""
fi

# Check available memory
echo "💾 Verificando memoria disponible..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    AVAILABLE_MEM=$(free -g | awk '/^Mem:/{print $7}')
    TOTAL_MEM=$(free -g | awk '/^Mem:/{print $2}')
    echo "   Memoria total: ${TOTAL_MEM}GB"
    echo "   Memoria disponible: ${AVAILABLE_MEM}GB"

    if [ "$AVAILABLE_MEM" -ge 8 ]; then
        echo "   ✅ Memoria suficiente (recomendado: 8GB+)"
    elif [ "$AVAILABLE_MEM" -ge 4 ]; then
        echo "   ⚠️  Memoria justa (disponible: ${AVAILABLE_MEM}GB, recomendado: 8GB+)"
        echo "      Los tests pueden ser lentos"
        WARNINGS=$((WARNINGS + 1))
    else
        echo "   ❌ Memoria insuficiente (disponible: ${AVAILABLE_MEM}GB, mínimo: 4GB)"
        ERRORS=$((ERRORS + 1))
    fi
elif [[ "$OSTYPE" == "darwin"* ]]; then
    TOTAL_MEM=$(sysctl -n hw.memsize | awk '{print int($1/1024/1024/1024)}')
    echo "   Memoria total: ${TOTAL_MEM}GB"
    if [ "$TOTAL_MEM" -ge 16 ]; then
        echo "   ✅ Memoria suficiente"
    else
        echo "   ⚠️  Memoria justa (recomendado: 16GB+ para macOS)"
        WARNINGS=$((WARNINGS + 1))
    fi
fi
echo ""

# Check disk space
echo "💿 Verificando espacio en disco..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    AVAILABLE_DISK=$(df -BG / | awk 'NR==2{print $4}' | sed 's/G//')
elif [[ "$OSTYPE" == "darwin"* ]]; then
    AVAILABLE_DISK=$(df -g / | awk 'NR==2{print $4}')
fi

echo "   Espacio disponible: ${AVAILABLE_DISK}GB"
if [ "$AVAILABLE_DISK" -ge 20 ]; then
    echo "   ✅ Espacio suficiente (recomendado: 20GB+)"
elif [ "$AVAILABLE_DISK" -ge 10 ]; then
    echo "   ⚠️  Espacio justo (disponible: ${AVAILABLE_DISK}GB, recomendado: 20GB+)"
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ❌ Espacio insuficiente (disponible: ${AVAILABLE_DISK}GB, mínimo: 10GB)"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check files
echo "📄 Verificando archivos necesarios..."
FILES=(
    "Dockerfile"
    "docker-compose.test.yml"
    "run-android-tests.sh"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $file"
    else
        echo "   ❌ $file NO encontrado"
        ERRORS=$((ERRORS + 1))
    fi
done
echo ""

# Check script permissions
echo "🔐 Verificando permisos..."
if [ -x "run-android-tests.sh" ]; then
    echo "   ✅ run-android-tests.sh tiene permisos de ejecución"
else
    echo "   ⚠️  run-android-tests.sh sin permisos de ejecución"
    echo "      Ejecuta: chmod +x run-android-tests.sh"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# Summary
echo "====================================================================="
echo "📊 RESUMEN:"
echo "====================================================================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "✅ ¡Todo listo! Puedes ejecutar los tests."
    echo ""
    echo "Para probar, ejecuta:"
    echo "   docker-compose -f docker-compose.test.yml up --build"
    echo ""
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "⚠️  Sistema listo con $WARNINGS advertencia(s)"
    echo "   Los tests deberían funcionar, pero podrían ser lentos"
    echo ""
    echo "Para probar, ejecuta:"
    echo "   docker-compose -f docker-compose.test.yml up --build"
    echo ""
    exit 0
else
    echo "❌ Se encontraron $ERRORS error(es) y $WARNINGS advertencia(s)"
    echo "   Por favor, resuelve los errores antes de continuar"
    echo ""
    exit 1
fi
