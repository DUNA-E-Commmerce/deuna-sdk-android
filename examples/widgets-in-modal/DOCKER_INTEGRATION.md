# DEUNA Android SDK - Docker Integration for e2e-pre-production

Este documento explica cómo los tests de integración de Android están configurados para ejecutarse en Docker y cómo integrarlos con el sistema `e2e-pre-production`.

## 📋 Archivos Creados

### 1. `Dockerfile`
Imagen Docker basada en el workflow de GitHub Actions que incluye:
- JDK 17
- Android SDK (API 31 y 34)
- Android Emulator
- Configuración KVM para aceleración de hardware
- Pre-descarga de dependencias Gradle

### 2. `docker-compose.yml`
Configuración de servicios para integrarse con la red `deuna-network`:
- Depende de: apigw, ema, merchant-auth, user-auth, payments, deuna-squid
- Configuración de proxy HTTP
- Volúmenes para reportes de tests
- Configuración de recursos (4 CPU, 8GB RAM)

### 3. `run-android-tests.sh`
Script de ejecución que:
- Inicia el emulador Android en modo headless
- Espera a que el emulador esté listo (con timeout)
- Deshabilita animaciones
- Ejecuta los tests: `./gradlew :widgets-in-modal:connectedAndroidTest`
- Copia los resultados a `/test-results/`
- Cleanup automático del emulador

### 4. Modificaciones en el código

#### `TestEnvironment.kt`
```kotlin
enum class TestEnvironment(val value: String) {
    STAGING("staging"),
    DEVELOPMENT("development"),
    PREPROD("preprod");  // ← NUEVO

    val apiEndpoint: String
        get() = when (this) {
            STAGING -> "https://api.stg.deuna.io"
            DEVELOPMENT -> "https://api.dev.deuna.io"
            PREPROD -> System.getenv("DEUNA_API_ENDPOINT") ?: "http://apigw:8080"
        }
}
```

#### `MerchantDataSource.kt`
```kotlin
object AdminCredentials {
    val username: String
        get() = System.getenv("ADMIN_USERNAME") ?: "developers@getduna.com"
    val password: String
        get() = System.getenv("ADMIN_PASSWORD") ?: "superadmin"
}
```

#### `DeunaSDKIntegrationTest.kt`
```kotlin
object Constants {
    val env: TestEnvironment = TestEnvironment.fromEnvironment()  // ← Lee de ENV
    val country: CountryCode = CountryCode.MX
}
```

### 5. `repository-list-entry.json`
Entrada completa para agregar a `e2e-pre-production/repository-list.json`

---

## 🚀 Cómo Probar Localmente

### Opción 1: Docker Compose Standalone
```bash
cd deuna-sdk-android/examples/widgets-in-modal

# Construir la imagen
docker-compose build

# Ejecutar los tests (requiere que los servicios de e2e-pre-production estén corriendo)
docker-compose up
```

### Opción 2: Integrado con e2e-pre-production

1. **Agregar la entrada a repository-list.json**:
   ```bash
   # En el directorio e2e-pre-production
   cd e2e-pre-production

   # Copiar la entrada del archivo repository-list-entry.json
   # y agregarla al array "repositories" en repository-list.json
   ```

2. **Agregar el contenedor a specific_containers_run** (si está usando filtro):
   ```json
   "specific_containers_run": [
     ...
     "deuna-sdk-android-integration-tests"
   ]
   ```

3. **Ejecutar el flujo completo**:
   ```bash
   cd e2e-pre-production
   make all
   ```

4. **Ver resultados**:
   ```bash
   # Los resultados estarán en:
   cd e2e-pre-production/repositories/deuna-sdk-android/examples/widgets-in-modal/test-results/

   # O si usaste depends_volumes:
   cd e2e-pre-production/android-test-reports/
   ```

---

## ⚙️ Variables de Entorno

| Variable | Descripción | Valor Default |
|----------|-------------|---------------|
| `DEUNA_API_ENDPOINT` | URL del API Gateway local | `http://apigw:8080` |
| `DEUNA_ENV` | Ambiente de prueba | `preprod` |
| `ADMIN_USERNAME` | Usuario admin para crear merchants | `developers@getduna.com` |
| `ADMIN_PASSWORD` | Contraseña del admin | `superadmin` |
| `EMULATOR_WAIT_TIME` | Timeout para el emulador (segundos) | `300` |
| `ANDROID_HOME` | Path del Android SDK | `/opt/android-sdk` |
| `JAVA_HOME` | Path del JDK | `/usr/lib/jvm/java-17-openjdk-amd64` |

---

## 🔧 Requisitos del Sistema

### Para ejecutar localmente:
- **Docker** con soporte para **KVM** (aceleración de hardware)
  - En Linux: `/dev/kvm` debe estar disponible
  - En macOS/Windows: Docker Desktop con virtualización habilitada
- **8GB RAM** disponibles para el contenedor
- **4 CPUs** recomendados
- **20GB** de espacio en disco

### Verificar KVM:
```bash
# En Linux
ls -l /dev/kvm
# Debería mostrar: crw-rw-rw- 1 root kvm ...

# Si no existe, instalar:
sudo apt-get install qemu-kvm
```

---

## 🐛 Troubleshooting

### Problema: "Emulator timeout"
**Causa**: El emulador no inicia en 5 minutos.
**Solución**:
1. Aumentar `EMULATOR_WAIT_TIME=600` (10 minutos)
2. Verificar logs: `docker logs deuna-sdk-android-tests`
3. Verificar que KVM esté disponible: `docker run --rm --privileged ubuntu ls -l /dev/kvm`

### Problema: "Tests fallan al conectarse a API"
**Causa**: Los servicios de backend no están listos o la red no está configurada.
**Solución**:
1. Verificar que los servicios estén corriendo: `docker ps | grep -E "apigw|ema|merchant-auth"`
2. Verificar conectividad: `docker exec deuna-sdk-android-tests curl http://apigw:8080/health`
3. Revisar la red: `docker network inspect deuna-network`

### Problema: "Out of memory"
**Causa**: El emulador y Gradle consumen mucha memoria.
**Solución**:
1. Aumentar memoria del contenedor en docker-compose.yml:
   ```yaml
   deploy:
     resources:
       limits:
         memory: 12G  # Aumentar de 8G a 12G
   ```
2. Limitar memoria del emulador en run-android-tests.sh:
   ```bash
   -memory 1536  # Reducir de 2048 a 1536
   ```

### Problema: "KVM not available"
**Causa**: Docker no tiene acceso a KVM.
**Solución**:
1. Verificar permisos: `sudo chmod 666 /dev/kvm`
2. Agregar usuario al grupo kvm: `sudo usermod -aG kvm $USER`
3. Reiniciar Docker: `sudo systemctl restart docker`

### Problema: "Gradle build fails"
**Causa**: Problemas de red o dependencias.
**Solución**:
1. Verificar proxy: `echo $http_proxy`
2. Limpiar cache Gradle:
   ```bash
   docker-compose run --rm deuna-sdk-android-integration-tests ./gradlew clean
   ```
3. Reconstruir imagen: `docker-compose build --no-cache`

---

## 📊 Tiempos de Ejecución Estimados

| Fase | Tiempo Estimado |
|------|-----------------|
| Build de imagen Docker (primera vez) | 10-15 min |
| Build de imagen Docker (con cache) | 2-3 min |
| Inicio del emulador | 2-3 min |
| Ejecución de tests | 3-5 min |
| **TOTAL (primera vez)** | **15-23 min** |
| **TOTAL (subsecuentes)** | **7-11 min** |

---

## 📝 Próximos Pasos

1. ✅ Probar localmente con `docker-compose up`
2. ✅ Verificar que los tests pasan
3. ✅ Agregar entrada a `e2e-pre-production/repository-list.json`
4. ✅ Ejecutar `make all` en e2e-pre-production
5. ✅ Configurar en CI/CD si es necesario

---

## 📚 Referencias

- Workflow de GitHub Actions: `.github/workflows/integration-tests.yml`
- Configuración del emulador: Similar a `reactivecircus/android-emulator-runner@v2`
- Documentación de e2e-pre-production: `e2e-pre-production/README.md`

---

## 🤝 Soporte

Si encuentras problemas o tienes preguntas:
1. Revisa los logs: `docker logs deuna-sdk-android-tests`
2. Revisa el log del emulador: `docker exec deuna-sdk-android-tests cat /tmp/emulator.log`
3. Contacta al equipo de DevOps

---

**Creado**: 2026-01-28
**Última actualización**: 2026-01-28
**Mantenedor**: DEUNA Engineering Team
