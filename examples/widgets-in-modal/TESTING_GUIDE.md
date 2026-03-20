# 🧪 Guía de Prueba - Tests de Android en Docker

Esta guía te ayudará a probar la implementación de Docker para los tests de Android **sin necesidad de e2e-pre-production**.

---

## 📋 Requisitos Previos

Antes de empezar, asegúrate de tener:
- ✅ **Docker** instalado y corriendo
- ✅ **Docker Compose** instalado
- ✅ **8GB+ RAM** disponible
- ✅ **20GB+** espacio en disco
- ✅ **KVM** (solo Linux) para aceleración de hardware

---

## 🚀 Pasos para Probar

### Paso 1: Verificar Requisitos del Sistema

Ejecuta el script de verificación:

```bash
cd deuna-sdk-android/examples/widgets-in-modal
./check-requirements.sh
```

**Salida esperada:**
```
✅ ¡Todo listo! Puedes ejecutar los tests.
```

Si hay errores, sigue las instrucciones que muestra el script.

---

### Paso 2: Construir la Imagen Docker

Este paso descarga todas las dependencias y crea la imagen Docker. **Tomará 10-15 minutos la primera vez**.

```bash
cd deuna-sdk-android/examples/widgets-in-modal

# Construir la imagen
docker-compose -f docker-compose.test.yml build
```

**Salida esperada:**
```
[+] Building 600.5s (XX/XX) FINISHED
 => [internal] load build definition from Dockerfile
 => => transferring dockerfile: 2.1kB
 => [internal] load .dockerignore
 ...
 => => naming to docker.io/library/widgets-in-modal-deuna-sdk-android-integration-tests
```

**Nota**: La primera vez descargará:
- JDK 17 (~200MB)
- Android SDK (~3GB)
- Android Emulator (~500MB)
- Dependencias Gradle (~500MB)

**Builds subsecuentes serán mucho más rápidos** gracias al cache de Docker.

---

### Paso 3: Ejecutar los Tests (Prueba Completa)

Este comando construye, inicia el emulador y ejecuta los tests. **Tomará 7-11 minutos**.

```bash
docker-compose -f docker-compose.test.yml up
```

**Lo que verás:**
```
🚀 Starting DEUNA Android SDK Integration Tests
================================================

🔧 Starting ADB server...
📱 Starting Android emulator...
   Emulator started with PID: 123

⏳ Waiting for emulator to be ready...
   Still waiting... (0/300 seconds)
   Still waiting... (5/300 seconds)
   ...
   ✅ Emulator is ready!

🎨 Disabling animations...
🔓 Granting permissions...

📊 Environment Information:
   DEUNA_API_ENDPOINT: https://api.stg.deuna.io
   DEUNA_ENV: staging
   ADMIN_USERNAME: developers@getduna.com

🧪 Running integration tests...
================================================

> Task :widgets-in-modal:connectedAndroidTest

DeunaSDKIntegrationTest > testPaymentWidgetSuccess[test_avd(AVD) - 12] PASSED

BUILD SUCCESSFUL in 5m 23s

================================================
✅ All tests passed!
================================================
```

---

### Paso 4: Verificar Resultados

Los resultados se guardan en el directorio local:

```bash
# Ver reportes HTML
open test-results/index.html

# O en Linux
xdg-open test-results/index.html

# Ver estructura de reportes
ls -la test-results/
```

**Estructura esperada:**
```
test-results/
├── index.html                        # Reporte principal
├── connected/
│   └── test_avd(AVD) - 12/
│       ├── index.html               # Resultados por dispositivo
│       └── com.deuna.sdkexample.integration.html
└── artifacts/
    └── connected/
        └── test_avd(AVD) - 12/
            ├── logcat-*.txt
            └── test-result.pb
```

---

### Paso 5: Limpiar Recursos

Después de terminar las pruebas:

```bash
# Detener y eliminar contenedores
docker-compose -f docker-compose.test.yml down

# Eliminar volúmenes (opcional, libera espacio)
docker-compose -f docker-compose.test.yml down -v

# Eliminar imagen (opcional, libera ~6GB)
docker rmi widgets-in-modal-deuna-sdk-android-integration-tests
```

---

## 🔍 Comandos Útiles para Debugging

### Ver logs en tiempo real
```bash
docker-compose -f docker-compose.test.yml logs -f
```

### Entrar al contenedor mientras corre
```bash
docker exec -it deuna-sdk-android-tests-standalone bash
```

### Ver procesos del emulador
```bash
docker exec -it deuna-sdk-android-tests-standalone ps aux | grep emulator
```

### Ver estado del emulador
```bash
docker exec -it deuna-sdk-android-tests-standalone adb devices
```

### Ver logs del emulador
```bash
docker exec -it deuna-sdk-android-tests-standalone cat /tmp/emulator.log
```

### Ejecutar Gradle manualmente (sin emulador)
```bash
docker-compose -f docker-compose.test.yml run --rm \
  --entrypoint bash \
  deuna-sdk-android-integration-tests

# Dentro del contenedor:
./gradlew :widgets-in-modal:assembleDebugAndroidTest
```

---

## 🧪 Modos de Prueba

### Modo 1: Prueba Rápida (Solo Build)

Verifica que la imagen se construye correctamente sin ejecutar tests:

```bash
# Solo construir
docker-compose -f docker-compose.test.yml build

# Verificar que la imagen existe
docker images | grep widgets-in-modal
```

---

### Modo 2: Prueba del Emulador

Inicia el contenedor y verifica que el emulador arranca:

```bash
docker-compose -f docker-compose.test.yml run --rm \
  --entrypoint bash \
  deuna-sdk-android-integration-tests

# Dentro del contenedor, inicia el emulador manualmente:
$ANDROID_HOME/emulator/emulator \
  -avd test_avd \
  -no-window \
  -gpu swiftshader_indirect \
  -noaudio \
  -no-boot-anim &

# Espera ~2 minutos y verifica:
adb devices
# Debería mostrar: emulator-5554    device
```

---

### Modo 3: Prueba Completa (Modo Normal)

```bash
docker-compose -f docker-compose.test.yml up
```

---

## ⚙️ Configuración Avanzada

### Cambiar el Ambiente de Prueba

Edita `docker-compose.test.yml` para usar diferentes ambientes:

```yaml
environment:
  # Para DEVELOPMENT
  - DEUNA_API_ENDPOINT=https://api.dev.deuna.io
  - DEUNA_ENV=development

  # Para STAGING (default)
  - DEUNA_API_ENDPOINT=https://api.stg.deuna.io
  - DEUNA_ENV=staging
```

### Aumentar Timeout del Emulador

Si el emulador tarda en arrancar:

```yaml
environment:
  - EMULATOR_WAIT_TIME=600  # 10 minutos en lugar de 5
```

### Ajustar Recursos

Si tu sistema tiene más/menos recursos:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'    # Reducir a 2 CPUs
      memory: 6G     # Reducir a 6GB RAM
```

---

## 🐛 Troubleshooting

### Error: "Cannot connect to the Docker daemon"
**Solución**: Inicia Docker Desktop o el daemon:
```bash
# macOS/Windows: Abre Docker Desktop

# Linux:
sudo systemctl start docker
```

---

### Error: "Device or resource busy: /dev/kvm"
**Solución**: Otro proceso está usando KVM:
```bash
# Ver qué proceso usa KVM
lsof /dev/kvm

# Detener otros emuladores
killall qemu-system-x86_64
```

---

### Error: "Emulator timeout"
**Posibles causas**:
1. **KVM no disponible** (Linux)
   ```bash
   # Verificar
   ls -l /dev/kvm

   # Dar permisos
   sudo chmod 666 /dev/kvm
   ```

2. **Recursos insuficientes**
   - Cierra otras aplicaciones
   - Aumenta memoria en docker-compose.yml

3. **Sistema muy lento**
   - Aumenta `EMULATOR_WAIT_TIME=600`

---

### Error: "Tests fail to connect to API"
**Causa**: Problemas de red o API caída.

**Solución**:
```bash
# Verifica que la API esté disponible
curl -I https://api.stg.deuna.io/health

# Si falla, usa DEVELOPMENT:
# Edita docker-compose.test.yml:
# DEUNA_API_ENDPOINT=https://api.dev.deuna.io
# DEUNA_ENV=development
```

---

### Error: "Out of memory" durante build
**Solución**: Limpia espacio y aumenta memoria de Docker:

```bash
# Limpiar imágenes no usadas
docker system prune -a

# En Docker Desktop:
# Settings → Resources → Memory → Aumentar a 8GB+
```

---

## ✅ Criterios de Éxito

La prueba es exitosa si:
- ✅ La imagen Docker se construye sin errores
- ✅ El emulador arranca en menos de 5 minutos
- ✅ Los tests se ejecutan y pasan
- ✅ Los reportes HTML se generan en `test-results/`
- ✅ El contenedor se detiene limpiamente

---

## 📊 Tiempos Esperados

| Acción | Primera Vez | Subsecuentes |
|--------|-------------|--------------|
| Build imagen | 10-15 min | 2-3 min |
| Inicio emulador | 2-3 min | 2-3 min |
| Ejecución tests | 3-5 min | 3-5 min |
| **TOTAL** | **15-23 min** | **7-11 min** |

---

## 🎯 Próximos Pasos

Una vez que los tests pasan localmente:

1. ✅ **Probar con diferentes ambientes** (development, staging)
2. ✅ **Integrar con e2e-pre-production**
3. ✅ **Agregar a CI/CD pipeline**
4. ✅ **Optimizar tiempos de ejecución**

---

## 📞 ¿Necesitas Ayuda?

Si los tests no pasan después de seguir esta guía:

1. Revisa los logs: `docker-compose -f docker-compose.test.yml logs`
2. Revisa el log del emulador: `docker exec deuna-sdk-android-tests-standalone cat /tmp/emulator.log`
3. Verifica el sistema: `./check-requirements.sh`
4. Contacta al equipo de DevOps

---

**¡Buena suerte con las pruebas! 🚀**
