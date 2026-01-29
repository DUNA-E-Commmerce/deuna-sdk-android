# DEUNA Android SDK - Configuración Docker para Ubuntu

Esta configuración permite ejecutar los tests de integración de Android en Docker en **Ubuntu con KVM**, optimizada para el workflow de e2e-pre-production.

## 🚀 Cómo usar

### Ejecución en Ubuntu (Producción)

```bash
cd deuna-sdk-android/examples/widgets-in-modal

# 1. Verificar que KVM esté disponible
ls -l /dev/kvm

# 2. Construir la imagen
docker-compose build

# 3. Ejecutar tests de integración
docker-compose up
```

### Integración con e2e-pre-production

El archivo `repository-list-entry.json` contiene la configuración completa para que el equipo de DevOps lo integre en `e2e-pre-production/repository-list.json`.

## 📁 Archivos esenciales

- **`Dockerfile`** - Imagen optimizada para Ubuntu con KVM
- **`docker-compose.yml`** - Configuración para Ubuntu con aceleración KVM
- **`run-android-tests.sh`** - Script principal que ejecuta los tests
- **`repository-list-entry.json`** - Para integración con e2e-pre-production

## ⚙️ Variables de entorno

| Variable | Descripción | Default Ubuntu |
|----------|-------------|----------------|
| `DEUNA_API_ENDPOINT` | URL del API | `http://apigw:8080` (e2e) |
| `DEUNA_ENV` | Ambiente | `preprod` |
| `ADMIN_USERNAME` | Usuario admin | `developers@getduna.com` |
| `ADMIN_PASSWORD` | Contraseña admin | `superadmin` |
| `EMULATOR_WAIT_TIME` | Timeout emulador (seg) | `180` (con KVM) |

## � Requisitos del sistema Ubuntu

- **Ubuntu 18.04+** con KVM habilitado
- **Docker** y **Docker Compose**
- **KVM** disponible en `/dev/kvm`
- **4GB RAM** mínimo, **8GB** recomendado
- **4 CPU cores** recomendado

### Verificar KVM en Ubuntu:
```bash
# Verificar que KVM esté disponible
ls -l /dev/kvm
# Debería mostrar: crw-rw-rw- 1 root kvm ...

# Si no existe, instalar:
sudo apt-get update
sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
sudo usermod -aG kvm $USER
sudo usermod -aG libvirt $USER
```

## 📊 Tiempos estimados (Ubuntu con KVM)

- **Build inicial**: 8-12 min
- **Build con cache**: 1-2 min  
- **Emulador (con KVM)**: 1-3 min
- **Tests completos**: 3-5 min
- **Total**: 10-15 min

## ✅ Configuración optimizada para

- ✅ Ubuntu 18.04+ con KVM
- ✅ GitHub Actions con Ubuntu runners
- ✅ e2e-pre-production workflow
- ✅ CI/CD pipelines en Linux

## 🔄 Para el equipo de DevOps

Usar el contenido de `repository-list-entry.json` para agregar al array `repositories` en `e2e-pre-production/repository-list.json`.

---

**Optimizado para**: Ubuntu con KVM  
**Creado**: 2026-01-29  
**Mantenedor**: DEUNA Engineering Team