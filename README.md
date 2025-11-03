# 🏛️ Sistema de Gestión de Reservas Municipales

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.2-blue?style=for-the-badge&logo=postgresql)
![JavaFX](https://img.shields.io/badge/JavaFX-21-red?style=for-the-badge&logo=java)
![License](https://img.shields.io/badge/License-Academic-yellow?style=for-the-badge)

**Sistema completo de reservas de espacios municipales con integración Azure AD**

[Características](#-características) •
[Instalación](#-instalación) •
[Uso](#-uso) •
[API](#-api-documentation) •
[Documentación](#-documentación)

</div>

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos](#-requisitos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [API Documentation](#-api-documentation)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## 🎯 Descripción

Sistema integral para la gestión de reservas de espacios municipales que incluye:

- **Backend REST API** construido con Spring Boot 3.5.6
- **Frontend Desktop** con JavaFX 21
- **Autenticación empresarial** con Azure Active Directory
- **Base de datos** PostgreSQL con Docker
- **Generación de códigos QR** para check-in
- **Notificaciones por email** con diseño premium
- **Exportación a Excel** de reportes
- **Panel de métricas y analytics**
- **Auditoría completa** de todas las operaciones

---

## ✨ Características

### 🔐 Seguridad
- ✅ Autenticación con Azure AD
- ✅ Tokens JWT con expiración configurable
- ✅ Control de acceso basado en roles (ADMIN, SUPERVISOR, USER)
- ✅ Auditoría completa de acciones (30 eventos)
- ✅ Soft delete para trazabilidad

### 📅 Gestión de Reservas
- ✅ Crear, aprobar, cancelar reservaciones
- ✅ Validación de horarios y capacidad
- ✅ Check-in con código QR
- ✅ Estados: PENDING, CONFIRMED, CANCELED, CHECKED_IN, NO_SHOW, COMPLETED
- ✅ Integración con OpenWeather API para espacios exteriores

### 📊 Reportes y Métricas
- ✅ Exportación a Excel (.xlsx)
- ✅ Estadísticas de ocupación por espacio
- ✅ Espacios más reservados (Top N)
- ✅ Distribución horaria de reservas
- ✅ Tasa de no-show por usuario
- ✅ Dashboard con métricas en tiempo real

### 🔍 Búsqueda Avanzada
- ✅ Filtro por tipo de espacio
- ✅ Filtro por rango de capacidad
- ✅ Búsqueda por ubicación (parcial)
- ✅ Filtro por estado activo/inactivo

### 📧 Notificaciones
- ✅ Emails automáticos al crear/aprobar/cancelar
- ✅ Diseño HTML responsive premium
- ✅ Códigos QR embebidos en emails
- ✅ Templates personalizables

### ⭐ Calificaciones
- ✅ Sistema de ratings (1-5 estrellas)
- ✅ Comentarios de usuarios
- ✅ Promedio de calificación por espacio
- ✅ Moderación de comentarios

---

## 🛠️ Tecnologías

### Backend
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.6 | Framework backend |
| Spring Security | 6.x | Seguridad y autenticación |
| Spring Data JPA | 3.x | Persistencia ORM |
| PostgreSQL | 16.2 | Base de datos |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| MapStruct | 1.5.5 | Mapeo de objetos |
| Apache POI | 5.2.5 | Exportación Excel |
| ZXing | 3.5.3 | Generación QR |
| SpringDoc OpenAPI | 2.8.9 | Documentación Swagger |
| Lombok | 1.18.36 | Reducción boilerplate |

### Frontend
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| JavaFX | 21 | Framework UI |
| Java HTTP Client | 21 | Cliente REST |
| Jackson | 2.18 | Procesamiento JSON |

### DevOps
- Docker & Docker Compose
- Maven 3.x
- Git

---

## 📦 Requisitos

### Software
- ✅ **Java JDK 21** o superior ([Descargar](https://www.oracle.com/java/technologies/downloads/))
- ✅ **Maven 3.8+** ([Descargar](https://maven.apache.org/download.cgi))
- ✅ **Docker & Docker Compose** ([Descargar](https://www.docker.com/products/docker-desktop))
- ✅ **Git** ([Descargar](https://git-scm.com/downloads))

### Cuentas
- ✅ **Azure AD Tenant** (para autenticación)
- ✅ **OpenWeather API Key** (gratis: https://openweathermap.org/api)
- ✅ **Cuenta Gmail** con App Password (para emails)

---

## 🚀 Instalación

### 1️⃣ Clonar Repositorio
```bash
git clone https://github.com/keylorpineda/projectcodexFX.git
cd projectcodexFX
```

### 2️⃣ Iniciar Base de Datos
```bash
docker-compose up -d
```

Verificar que PostgreSQL está corriendo:
```bash
docker ps
```

### 3️⃣ Configurar Backend

Editar `src/main/resources/application.properties`:

```properties
# ==================== DATABASE ====================
spring.datasource.url=jdbc:postgresql://localhost:5432/municipal_reservations
spring.datasource.username=admin_user
spring.datasource.password=admin123

# ==================== JWT ====================
jwt.secret=CAMBIA_ESTO_POR_UN_SECRET_SUPER_SEGURO_DE_AL_MENOS_256_BITS
jwt.expiration=86400000

# ==================== AZURE AD ====================
azure.ad.tenant-id=TU_TENANT_ID_AQUI
azure.ad.client-id=TU_CLIENT_ID_AQUI

# ==================== OPENWEATHER ====================
openweather.api.key=TU_OPENWEATHER_API_KEY_AQUI
openweather.api.base-url=https://api.openweathermap.org/data/2.5

# ==================== EMAIL ====================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password-aqui
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 4️⃣ Compilar e Iniciar Backend
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

El backend estará disponible en: **http://localhost:8080**

### 5️⃣ Configurar Frontend

Navegar al módulo JavaFX:
```bash
cd municipal-admin-fx
```

Editar `src/main/resources/config/application.properties`:
```properties
api.base.url=http://localhost:8080/api
```

### 6️⃣ Ejecutar Frontend
```bash
mvn clean javafx:run
```

---

## ⚙️ Configuración

### Azure AD Setup

1. Ir a [Azure Portal](https://portal.azure.com)
2. **Azure Active Directory** → **App registrations** → **New registration**
3. Nombre: `Municipal Reservations System`
4. Supported account types: `Accounts in this organizational directory only`
5. Redirect URI: `http://localhost:8080/api/auth/callback`
6. Copiar **Application (client) ID** → `azure.ad.client-id`
7. Copiar **Directory (tenant) ID** → `azure.ad.tenant-id`

### Gmail App Password

1. Ir a [Google Account](https://myaccount.google.com/)
2. **Security** → **2-Step Verification** (activar si no está)
3. **App passwords** → Generar
4. Copiar password → `spring.mail.password`

### OpenWeather API

1. Crear cuenta en [OpenWeather](https://openweathermap.org/api)
2. **API Keys** → Copiar key
3. Pegar en `openweather.api.key`

---

## 💻 Uso

### Login
1. Ejecutar aplicación JavaFX
2. Clic en **"Login with Azure AD"**
3. Ingresar credenciales corporativas
4. Redirigido a dashboard según rol

### Crear Reserva (Usuario)
1. Dashboard → **"Nueva Reserva"**
2. Seleccionar espacio
3. Elegir fecha y hora
4. Especificar asistentes
5. Clic en **"Reservar"**
6. Recibirás email con código QR

### Aprobar Reserva (Supervisor/Admin)
1. Dashboard → **"Reservas Pendientes"**
2. Revisar detalles
3. Clic en **"Aprobar"** o **"Rechazar"**
4. Usuario recibe notificación

### Exportar a Excel
1. Dashboard → **"Exportar Reportes"**
2. Seleccionar tipo:
   - Mis reservaciones
   - Todas las reservaciones
   - Estadísticas de espacios
3. Archivo .xlsx se descarga automáticamente

### Ver Métricas (Admin)
1. Dashboard → **"Analytics"**
2. Ver:
   - Tasa de ocupación
   - Espacios más reservados
   - Distribución horaria
   - No-shows

---

## 📚 API Documentation

### Swagger UI
Una vez el backend esté corriendo, acceder a:

**http://localhost:8080/swagger-ui.html**

### Endpoints Principales

#### Autenticación
```http
POST   /api/auth/login        # Login con Azure AD
POST   /api/auth/refresh      # Refrescar token JWT
```

#### Reservaciones
```http
POST   /api/reservations                      # Crear reserva
GET    /api/reservations                      # Listar reservas
GET    /api/reservations/{id}                 # Obtener reserva
PUT    /api/reservations/{id}                 # Actualizar reserva
DELETE /api/reservations/{id}                 # Eliminar reserva
POST   /api/reservations/{id}/cancel          # Cancelar reserva
POST   /api/reservations/{id}/approve         # Aprobar reserva
POST   /api/reservations/{id}/check-in        # Check-in
GET    /api/reservations/export               # Exportar Excel
```

#### Espacios
```http
POST   /api/spaces            # Crear espacio
GET    /api/spaces            # Listar espacios
GET    /api/spaces/search     # Búsqueda avanzada 🆕
GET    /api/spaces/{id}       # Obtener espacio
PUT    /api/spaces/{id}       # Actualizar espacio
DELETE /api/spaces/{id}       # Eliminar espacio
```

#### Analytics (Nuevo) 🆕
```http
GET    /api/analytics/occupancy-by-space      # Tasa ocupación
GET    /api/analytics/top-spaces              # Top espacios
GET    /api/analytics/reservations-by-hour    # Distribución horaria
GET    /api/analytics/no-show-rate-by-user    # Tasa no-show
GET    /api/analytics/system-statistics       # Stats generales
```

### Ejemplo de Request

```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "spaceId": 3,
    "startTime": "2025-11-15T10:00:00",
    "endTime": "2025-11-15T12:00:00",
    "attendees": 25,
    "notes": "Reunión departamental"
  }'
```

---

## 📁 Estructura del Proyecto

```
projectcodex/
├── src/main/java/finalprojectprogramming/project/
│   ├── controllers/          # REST Controllers
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── SpaceController.java
│   │   ├── ReservationController.java
│   │   ├── AnalyticsController.java 🆕
│   │   └── ...
│   ├── services/             # Business Logic
│   │   ├── user/
│   │   ├── space/
│   │   ├── reservation/
│   │   ├── analytics/ 🆕
│   │   ├── excel/ 🆕
│   │   └── ...
│   ├── repositories/         # Data Access (JPA)
│   ├── models/               # JPA Entities
│   ├── dtos/                 # Data Transfer Objects
│   ├── security/             # JWT, Azure AD, Hashing
│   ├── exceptions/           # Custom Exceptions
│   ├── transformers/         # Entity ↔ DTO
│   └── configs/              # Configuration
├── municipal-admin-fx/       # JavaFX Frontend
│   ├── src/main/java/com/municipal/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── ui/
│   │   └── utils/
│   └── src/main/resources/
│       └── com/municipal/reservationsfx/ui/
├── docs/ 🆕                  # Documentación
│   └── DOCUMENTACION_TECNICA.md
├── docker-compose.yml        # PostgreSQL + pgAdmin
├── pom.xml                   # Maven (Backend)
└── README.md                 # Este archivo
```

---

## 🧪 Testing

### Ejecutar Tests
```bash
mvn test
```

### Cobertura en terminal (una línea)
Opciones para ver el % de cobertura directamente en la consola:

1) Makefile (recomendado)
```bash
make coverage
```

2) VS Code Task
- Abrir la paleta (⇧⌘P) → "Run Task" → seleccionar "coverage".

3) Maven solamente
```bash
./mvnw -q -DskipITs -Djacoco.skip=false -Dgpg.skip -T1C test jacoco:report exec:exec
```

Salida esperada en consola (ejemplo):
```
Instrucciones cubiertas: 97.08%
Líneas cubiertas: 98.28%
```

Además, el reporte HTML completo queda en: `target/site/jacoco/index.html`

### Objetivo
> **Cobertura mínima requerida: 70%**

---

## 🤝 Contribución

Este es un proyecto académico para el curso EIF206 - Programación III de la Universidad Nacional de Costa Rica.

### Autor
**Keylor Pineda**  
Universidad Nacional de Costa Rica  
Ingeniería en Sistemas de Información

---

## 📄 Licencia

**Proyecto Académico** - Universidad Nacional de Costa Rica  
**Curso:** EIF206 - Programación III  
**Período:** III Ciclo 2024  
**Fecha:** Noviembre 3, 2025

---

## 📞 Soporte

Para preguntas o problemas:

- **Email:** [tu-email@universidad.cr]
- **Repositorio:** [https://github.com/keylorpineda/projectcodexFX](https://github.com/keylorpineda/projectcodexFX)
- **Swagger:** http://localhost:8080/swagger-ui.html

---

## 🌟 Features Destacadas

- ✅ **100% Auditoría** - 30 eventos auditados en 8 servicios
- ✅ **QR Codes** - Generación y envío automático en emails
- ✅ **Excel Export** - 3 tipos de reportes profesionales
- ✅ **Analytics Dashboard** - Métricas en tiempo real
- ✅ **Advanced Search** - Búsqueda multi-criterio
- ✅ **Email Premium** - Diseño HTML responsive con gradientes
- ✅ **JWT + Azure AD** - Autenticación empresarial
- ✅ **Swagger Complete** - Documentación de 60+ endpoints

---

<div align="center">

**Desarrollado con ❤️ para la Universidad Nacional de Costa Rica**

![Universidad Nacional](https://img.shields.io/badge/UNA-Costa%20Rica-blue?style=for-the-badge)

</div>
