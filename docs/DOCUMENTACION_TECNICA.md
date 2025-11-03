# 📋 Sistema de Gestión de Reservas Municipales - Documentación Técnica

## 📌 Información del Proyecto

**Universidad:** Universidad Nacional de Costa Rica  
**Carrera:** Ingeniería en Sistemas de Información  
**Curso:** EIF206 - Programación III  
**Período:** III Ciclo 2024  
**Estudiante:** [Nombre del estudiante]  
**Profesor:** [Nombre del profesor]  
**Fecha:** Noviembre 3, 2025  

---

## 📖 Índice

1. [Descripción General](#1-descripción-general)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Tecnologías Utilizadas](#3-tecnologías-utilizadas)
4. [Patrones de Diseño](#4-patrones-de-diseño)
5. [Modelo de Datos](#5-modelo-de-datos)
6. [API REST](#6-api-rest)
7. [Seguridad](#7-seguridad)
8. [Características Principales](#8-características-principales)
9. [Instalación y Configuración](#9-instalación-y-configuración)
10. [Manual de Usuario](#10-manual-de-usuario)
11. [Pruebas](#11-pruebas)
12. [Limitaciones y Trabajo Futuro](#12-limitaciones-y-trabajo-futuro)

---

## 1. Descripción General

El **Sistema de Gestión de Reservas Municipales** es una aplicación completa para administrar la reservación de espacios públicos municipales. El sistema permite a los usuarios reservar espacios, verificar disponibilidad, recibir notificaciones por correo con códigos QR, y a los administradores gestionar espacios, usuarios y generar reportes.

### 1.1 Objetivos del Sistema

- ✅ Facilitar la reserva de espacios municipales de forma digital
- ✅ Automatizar el proceso de aprobación y gestión de reservas
- ✅ Proporcionar trazabilidad mediante auditoría completa
- ✅ Generar reportes y estadísticas en formato Excel
- ✅ Integración con Azure AD para autenticación empresarial
- ✅ Notificaciones automáticas por correo electrónico con códigos QR
- ✅ Verificación de condiciones climáticas mediante OpenWeather API

### 1.2 Alcance

El sistema cuenta con tres tipos de usuarios:
- **ADMIN:** Control total del sistema
- **SUPERVISOR:** Gestión de reservas y espacios
- **USER:** Creación y gestión de propias reservas

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura de Capas

El sistema sigue una arquitectura de **3 capas** (Three-Tier Architecture):

```
┌─────────────────────────────────────────┐
│    CAPA DE PRESENTACIÓN                 │
│  - JavaFX Desktop Client                │
│  - Admin Dashboard                      │
│  - Supervisor Dashboard                 │
│  - User Dashboard                       │
└─────────────────────────────────────────┘
              ↕ HTTP/REST
┌─────────────────────────────────────────┐
│    CAPA DE NEGOCIO (Backend)           │
│  - Spring Boot 3.5.6                   │
│  - Controllers (REST API)              │
│  - Services (Business Logic)           │
│  - Repositories (Data Access)          │
│  - Security (JWT + Azure AD)           │
└─────────────────────────────────────────┘
              ↕ JPA/Hibernate
┌─────────────────────────────────────────┐
│    CAPA DE DATOS                        │
│  - PostgreSQL 16.2                     │
│  - Docker Container                     │
└─────────────────────────────────────────┘
```

### 2.2 Componentes Principales

#### Backend (Spring Boot)
- **Controllers:** Endpoints REST API
- **Services:** Lógica de negocio
- **Repositories:** Acceso a datos con JPA
- **Security:** Autenticación JWT + Azure AD
- **DTOs:** Objetos de transferencia de datos
- **Models:** Entidades JPA
- **Transformers:** Conversión Entity ↔ DTO

#### Frontend (JavaFX)
- **Controllers:** Lógica de vistas
- **Services:** Comunicación con API REST
- **DTOs:** Objetos de datos locales
- **Utils:** Utilidades (caché, JSON, HTTP)
- **UI Components:** Componentes reutilizables

---

## 3. Tecnologías Utilizadas

### 3.1 Backend

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.5.6 | Framework principal |
| **Spring Security** | 6.x | Seguridad y autenticación |
| **Spring Data JPA** | 3.x | Persistencia de datos |
| **Hibernate** | 6.x | ORM |
| **PostgreSQL** | 16.2 | Base de datos |
| **JWT (jjwt)** | 0.11.5 | Tokens de autenticación |
| **ModelMapper** | 3.2.0 | Conversión DTO/Entity |
| **Lombok** | 1.18.36 | Reducción de boilerplate |
| **Apache POI** | 5.2.5 | Exportación Excel |
| **ZXing** | 3.5.3 | Generación códigos QR |
| **SpringDoc OpenAPI** | 2.8.9 | Documentación Swagger |
| **Jakarta Mail** | 2.0.1 | Envío de correos |
| **Jackson** | 2.17.1 | Procesamiento JSON |
| **SLF4J + Log4j2** | 2.x | Logging |

### 3.2 Frontend

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **JavaFX** | 21.0.2 | Framework UI |
| **Maven** | 3.x | Gestión de dependencias |
| **Java HTTP Client** | 21 | Cliente REST |
| **Jackson** | 2.17.1 | Procesamiento JSON |

### 3.3 Herramientas de Desarrollo

- **Maven:** Gestión de proyecto y dependencias
- **Git:** Control de versiones
- **Docker & Docker Compose:** Contenedorización
- **VS Code:** Editor de código
- **Postman:** Testing de API
- **DBeaver:** Cliente PostgreSQL

---

## 4. Patrones de Diseño

### 4.1 Patrones Implementados

#### 4.1.1 Repository Pattern
**Ubicación:** `repositories/`  
**Propósito:** Abstracción del acceso a datos

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

#### 4.1.2 Service Layer Pattern
**Ubicación:** `services/`  
**Propósito:** Encapsulación de lógica de negocio

```java
@Service
@Transactional
public class UserServiceImplementation implements UserService {
    // Business logic here
}
```

#### 4.1.3 DTO Pattern
**Ubicación:** `dtos/`  
**Propósito:** Transferencia de datos entre capas

```java
public record UserOutputDTO(
    Long id,
    String name,
    String email,
    UserRole role,
    Boolean active
) {}
```

#### 4.1.4 Strategy Pattern
**Ubicación:** `security/hash/`  
**Propósito:** Algoritmos de hashing intercambiables

```java
public interface HashService {
    String hash(String password);
    boolean verify(String password, String hashedPassword);
}
```

#### 4.1.5 Builder Pattern
**Ubicación:** Entidades con `@Builder` de Lombok

```java
@Builder
@Entity
public class Reservation {
    // Permite crear objetos complejos paso a paso
}
```

#### 4.1.6 Singleton Pattern
**Ubicación:** `SessionManager`, Services con `@Service`  
**Propósito:** Única instancia de servicios

```java
@Service
public class AuditLogService {
    // Spring garantiza singleton por defecto
}
```

#### 4.1.7 MVC Pattern
**Ubicación:** Controllers y JavaFX  
**Propósito:** Separación de responsabilidades
- **Model:** Entidades y DTOs
- **View:** FXML files
- **Controller:** Controllers (Spring y JavaFX)

---

## 5. Modelo de Datos

### 5.1 Diagrama Entidad-Relación

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    USERS     │         │ RESERVATIONS │         │   SPACES     │
├──────────────┤         ├──────────────┤         ├──────────────┤
│ id (PK)      │◄────────│ user_id (FK) │────────►│ id (PK)      │
│ email        │         │ space_id(FK) │         │ name         │
│ name         │         │ start_time   │         │ type         │
│ role         │         │ end_time     │         │ capacity     │
│ active       │         │ status       │         │ location     │
│ created_at   │         │ qr_code      │         │ description  │
│ updated_at   │         │ checkin_at   │         │ active       │
│ deleted_at   │         │ canceled_at  │         │ created_at   │
└──────────────┘         │ approved_by  │         │ updated_at   │
       │                 │ attendees    │         │ deleted_at   │
       │                 │ notes        │         └──────────────┘
       │                 │ created_at   │                │
       │                 │ updated_at   │                │
       │                 │ deleted_at   │                │
       │                 └──────────────┘                │
       │                        │                        │
       │                        │                        │
       │                        │                        │
       │                 ┌──────────────┐                │
       │                 │   RATINGS    │                │
       │                 ├──────────────┤                │
       │                 │ id (PK)      │                │
       │                 │ reserv_id(FK)│                │
       │                 │ score        │                │
       │                 │ comment      │                │
       │                 │ is_visible   │                │
       │                 │ created_at   │                │
       │                 └──────────────┘                │
       │                                                 │
       │                 ┌──────────────┐                │
       │                 │ AUDIT_LOGS   │                │
       │                 ├──────────────┤                │
       └────────────────►│ user_id (FK) │                │
                         │ action       │                │
                         │ entity_id    │                │
                         │ details      │                │
                         │ created_at   │                │
                         └──────────────┘                │
                                                         │
                         ┌──────────────┐                │
                         │SPACE_IMAGES  │                │
                         ├──────────────┤                │
                         │ id (PK)      │                │
                         │ space_id(FK) │◄───────────────┘
                         │ file_name    │
                         │ file_type    │
                         │ file_data    │
                         │ is_primary   │
                         └──────────────┘

                         ┌──────────────┐
                         │SPACE_SCHEDULE│
                         ├──────────────┤
                         │ id (PK)      │
                         │ space_id(FK) │◄───────────────┘
                         │ day_of_week  │
                         │ open_time    │
                         │ close_time   │
                         │ is_available │
                         └──────────────┘

                         ┌──────────────┐
                         │NOTIFICATIONS │
                         ├──────────────┤
                         │ id (PK)      │
                         │ user_id (FK) │◄───────────────┐
                         │ title        │                │
                         │ message      │                │
                         │ is_read      │                │
                         │ created_at   │                │
                         └──────────────┘                │

                         ┌──────────────┐
                         │  SETTINGS    │
                         ├──────────────┤
                         │ id (PK)      │
                         │ key_name     │
                         │ value        │
                         │ description  │
                         │ created_at   │
                         └──────────────┘
```

### 5.2 Entidades Principales

#### 5.2.1 User
**Tabla:** `users`  
**Propósito:** Almacenar información de usuarios del sistema

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | Identificador único (PK) |
| email | VARCHAR(255) | Email único del usuario |
| name | VARCHAR(100) | Nombre completo |
| role | VARCHAR(50) | ADMIN / SUPERVISOR / USER |
| active | BOOLEAN | Estado activo/inactivo |
| last_login_at | TIMESTAMP | Último inicio de sesión |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Fecha de actualización |
| deleted_at | TIMESTAMP | Soft delete |

#### 5.2.2 Space
**Tabla:** `spaces`  
**Propósito:** Espacios reservables

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | Identificador único (PK) |
| name | VARCHAR(255) | Nombre del espacio |
| type | VARCHAR(50) | AUDITORIUM / MEETING_ROOM / LAB / SPORTS_FIELD / PARK |
| capacity | INTEGER | Capacidad máxima |
| location | VARCHAR(500) | Ubicación física |
| description | TEXT | Descripción detallada |
| active | BOOLEAN | Disponible para reservas |
| features | TEXT | Características especiales |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Fecha de actualización |
| deleted_at | TIMESTAMP | Soft delete |

#### 5.2.3 Reservation
**Tabla:** `reservations`  
**Propósito:** Reservaciones de espacios

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | Identificador único (PK) |
| user_id | BIGINT | Usuario que reserva (FK) |
| space_id | BIGINT | Espacio reservado (FK) |
| start_time | TIMESTAMP | Inicio de reserva |
| end_time | TIMESTAMP | Fin de reserva |
| status | VARCHAR(50) | PENDING/CONFIRMED/CANCELED/CHECKED_IN/NO_SHOW/COMPLETED |
| qr_code | VARCHAR(255) | Código QR único |
| checkin_at | TIMESTAMP | Fecha de check-in |
| canceled_at | TIMESTAMP | Fecha de cancelación |
| approved_by | BIGINT | Usuario que aprobó (FK) |
| attendees | INTEGER | Número de asistentes |
| notes | TEXT | Notas adicionales |
| weather_check | JSON | Datos climáticos |
| cancellation_reason | TEXT | Razón de cancelación |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Fecha de actualización |
| deleted_at | TIMESTAMP | Soft delete |

---

## 6. API REST

### 6.1 Endpoints Principales

#### 6.1.1 Autenticación
```
POST   /api/auth/login        - Login con Azure AD
POST   /api/auth/refresh      - Refrescar token JWT
```

#### 6.1.2 Usuarios
```
POST   /api/users             - Crear usuario
GET    /api/users             - Listar usuarios
GET    /api/users/{id}        - Obtener usuario
PUT    /api/users/{id}        - Actualizar usuario
DELETE /api/users/{id}        - Eliminar usuario (soft)
```

#### 6.1.3 Espacios
```
POST   /api/spaces            - Crear espacio
GET    /api/spaces            - Listar espacios
GET    /api/spaces/search     - Búsqueda avanzada 🆕
GET    /api/spaces/{id}       - Obtener espacio
PUT    /api/spaces/{id}       - Actualizar espacio
DELETE /api/spaces/{id}       - Eliminar espacio
PATCH  /api/spaces/{id}/status - Cambiar estado
GET    /api/spaces/available  - Espacios disponibles
```

#### 6.1.4 Reservaciones
```
POST   /api/reservations                      - Crear reserva
GET    /api/reservations                      - Listar reservas
GET    /api/reservations/{id}                 - Obtener reserva
PUT    /api/reservations/{id}                 - Actualizar reserva
DELETE /api/reservations/{id}                 - Eliminar reserva
POST   /api/reservations/{id}/cancel          - Cancelar reserva
POST   /api/reservations/{id}/approve         - Aprobar reserva
POST   /api/reservations/{id}/check-in        - Registrar check-in
POST   /api/reservations/{id}/no-show         - Marcar no-show
GET    /api/reservations/export               - Exportar a Excel 🆕
GET    /api/reservations/export/space-statistics - Estadísticas Excel 🆕
```

#### 6.1.5 Analytics (Nuevo) 🆕
```
GET    /api/analytics/occupancy-by-space      - Tasa de ocupación
GET    /api/analytics/top-spaces              - Espacios más reservados
GET    /api/analytics/reservations-by-hour    - Distribución horaria
GET    /api/analytics/no-show-rate-by-user    - Tasa de no-show
GET    /api/analytics/system-statistics       - Estadísticas generales
GET    /api/analytics/reservations-by-status  - Reservas por estado
```

#### 6.1.6 Auditoría
```
GET    /api/audit-logs        - Logs de auditoría
GET    /api/audit-logs/user/{id} - Logs por usuario
```

#### 6.1.7 Calificaciones
```
POST   /api/ratings           - Crear calificación
GET    /api/ratings/space/{id} - Calificaciones de espacio
PUT    /api/ratings/{id}      - Actualizar calificación
DELETE /api/ratings/{id}      - Eliminar calificación
```

### 6.2 Ejemplo de Request/Response

#### Crear Reservación
**Request:**
```http
POST /api/reservations
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "userId": 1,
  "spaceId": 3,
  "startTime": "2025-11-15T10:00:00",
  "endTime": "2025-11-15T12:00:00",
  "attendees": 25,
  "notes": "Reunión departamental"
}
```

**Response:**
```json
{
  "id": 45,
  "userId": 1,
  "userName": "Juan Pérez",
  "spaceId": 3,
  "spaceName": "Auditorio Principal",
  "startTime": "2025-11-15T10:00:00",
  "endTime": "2025-11-15T12:00:00",
  "status": "PENDING",
  "qrCode": "QR-45-abc123def456",
  "attendees": 25,
  "notes": "Reunión departamental",
  "createdAt": "2025-11-03T01:00:00"
}
```

---

## 7. Seguridad

### 7.1 Autenticación

#### 7.1.1 Azure AD Integration
El sistema se integra con Azure Active Directory para autenticación empresarial:

```java
@PostMapping("/login")
public ResponseEntity<AuthResponseDTO> login(@RequestBody AzureLoginRequestDTO request) {
    // Valida token de Azure AD
    // Genera JWT propio del sistema
    // Retorna token + información de usuario
}
```

#### 7.1.2 JWT (JSON Web Tokens)
- **Librería:** io.jsonwebtoken (jjwt)
- **Algoritmo:** HS256
- **Expiración:** Configurable (default 24 horas)
- **Secret:** Almacenado en `application.properties`

**Estructura del Token:**
```json
{
  "sub": "user@example.com",
  "userId": 1,
  "role": "ADMIN",
  "iat": 1699000000,
  "exp": 1699086400
}
```

### 7.2 Autorización

#### 7.2.1 Roles y Permisos

| Rol | Permisos |
|-----|----------|
| **ADMIN** | Acceso completo al sistema |
| **SUPERVISOR** | Gestión de reservas y espacios |
| **USER** | Crear y gestionar propias reservas |

#### 7.2.2 Anotaciones de Seguridad

```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // Solo ADMIN puede eliminar usuarios
}

@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
public ResponseEntity<List<SpaceDTO>> getAllSpaces() {
    // Todos los roles autenticados pueden ver espacios
}
```

### 7.3 Protección de Datos

#### 7.3.1 Hashing de Contraseñas (Si aplica)
- **Algoritmo:** BCrypt
- **Trabajo Factor:** 10 rounds
- **Salt:** Generado automáticamente

#### 7.3.2 Soft Delete
Todas las entidades principales implementan soft delete para mantener trazabilidad:
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;
```

### 7.4 CORS Configuration

```java
@Configuration
public class WebConfig {
    @Bean
    public CorsFilter corsFilter() {
        // Permite origen del frontend JavaFX
        // Permite credenciales
        // Métodos: GET, POST, PUT, DELETE, PATCH
    }
}
```

---

## 8. Características Principales

### 8.1 Gestión de Reservaciones

#### 8.1.1 Flujo de Reserva
1. Usuario crea reservación → Estado: `PENDING`
2. Supervisor/Admin aprueba → Estado: `CONFIRMED`
3. Usuario hace check-in → Estado: `CHECKED_IN`
4. Finaliza reserva → Estado: `COMPLETED`

#### 8.1.2 Estados de Reservación
- **PENDING:** Esperando aprobación
- **CONFIRMED:** Aprobada por supervisor
- **CANCELED:** Cancelada por usuario/admin
- **CHECKED_IN:** Usuario confirmó asistencia
- **NO_SHOW:** Usuario no asistió
- **COMPLETED:** Reserva finalizada

#### 8.1.3 Validaciones
✅ Sin superposición de horarios  
✅ Espacio activo y disponible  
✅ Capacidad suficiente  
✅ Horario dentro de disponibilidad del espacio  
✅ Verificación climática para espacios exteriores  

### 8.2 Códigos QR

#### 8.2.1 Generación
- **Librería:** ZXing (Zebra Crossing)
- **Formato:** PNG, 250x250 px
- **Contenido:** Código único de reservación
- **Corrección de errores:** Nivel H (30%)

```java
public byte[] generateQRCodeImage(String text, int width, int height) {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
    // Convierte a imagen PNG
}
```

#### 8.2.2 Uso
- Enviado por email al crear/aprobar reserva
- Embebido en HTML como data URI
- Usado para check-in presencial

### 8.3 Sistema de Correos

#### 8.3.1 Tipos de Emails
1. **Reserva Creada:** Confirmación con QR
2. **Reserva Aprobada:** Notificación con QR actualizado
3. **Reserva Cancelada:** Aviso de cancelación
4. **Correos Personalizados:** Por administradores

#### 8.3.2 Características
- **HTML Responsive:** Adaptado a móviles
- **Diseño Premium:** Gradientes, colores corporativos
- **QR Embebido:** Imagen inline, no adjunta
- **Información Completa:** Espacio, horario, código, instrucciones

### 8.4 Exportación a Excel 🆕

#### 8.4.1 Reportes Disponibles
1. **Mis Reservaciones:** Usuario exporta su historial
2. **Todas las Reservaciones:** Admin exporta todo (con filtros)
3. **Estadísticas de Espacios:** Métricas de ocupación

#### 8.4.2 Características
- **Formato:** .xlsx (Excel 2007+)
- **Estilos:** Cabeceras con fondo azul, fuente blanca
- **Auto-ajuste:** Columnas ajustadas automáticamente
- **Datos:** Nombres amigables, fechas formateadas

### 8.5 Panel de Métricas 🆕

#### 8.5.1 Estadísticas Disponibles
- **Tasa de Ocupación:** Por espacio (0-100%)
- **Espacios Más Reservados:** Top N con detalles
- **Distribución Horaria:** Picos de reservas por hora
- **Tasa de No-Show:** Por usuario
- **Estadísticas Generales:** Users, spaces, reservations, promedios

#### 8.5.2 Uso
- Endpoint REST para JavaFX dashboard
- Datos en tiempo real
- Filtros por período (futuro)

### 8.6 Búsqueda Avanzada 🆕

#### 8.6.1 Filtros Disponibles
- **Tipo de Espacio:** AUDITORIUM, MEETING_ROOM, etc.
- **Capacidad Mínima:** Filtro >= capacidad
- **Capacidad Máxima:** Filtro <= capacidad
- **Ubicación:** Búsqueda parcial case-insensitive
- **Estado:** Activo/Inactivo

#### 8.6.2 Ejemplo de Uso
```
GET /api/spaces/search?type=AUDITORIUM&minCapacity=50&location=centro&active=true
```

### 8.7 Auditoría Completa (100%) ✅

#### 8.7.1 Servicios Auditados
1. **ReservationService:** 8 eventos
2. **UserService:** 3 eventos
3. **SpaceService:** 3 eventos
4. **RatingService:** 4 eventos
5. **SpaceImageService:** 4 eventos
6. **SpaceScheduleService:** 3 eventos
7. **SettingService:** 3 eventos
8. **NotificationService:** 2 eventos

**Total:** 30 eventos de auditoría

#### 8.7.2 Información Registrada
- Usuario que ejecuta la acción
- Tipo de acción (CREATE, UPDATE, DELETE, etc.)
- ID de entidad afectada
- Detalles en formato JSON
- Timestamp

### 8.8 Integración OpenWeather API

#### 8.8.1 Propósito
Verificar condiciones climáticas para espacios exteriores antes de aprobar reservas.

#### 8.8.2 Datos Obtenidos
- Temperatura
- Condición climática (lluvia, nublado, etc.)
- Descripción
- Almacenado en campo JSON de Reservation

---

## 9. Instalación y Configuración

### 9.1 Requisitos Previos

#### 9.1.1 Software Necesario
- **Java JDK:** 21 o superior
- **Maven:** 3.8 o superior
- **Docker & Docker Compose:** Para PostgreSQL
- **PostgreSQL:** 16.2 (o vía Docker)
- **Git:** Para clonar repositorio
- **Node.js:** (opcional, para herramientas)

#### 9.1.2 Cuentas Necesarias
- **Azure AD:** Tenant configurado
- **OpenWeather API:** Key gratuita

### 9.2 Instalación Backend

#### 9.2.1 Clonar Repositorio
```bash
git clone https://github.com/keylorpineda/projectcodexFX.git
cd projectcodexFX
```

#### 9.2.2 Configurar Base de Datos
Opción 1 - Docker (Recomendado):
```bash
docker-compose up -d
```

Opción 2 - PostgreSQL Local:
```sql
CREATE DATABASE municipal_reservations;
CREATE USER admin_user WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE municipal_reservations TO admin_user;
```

#### 9.2.3 Configurar `application.properties`
Editar: `src/main/resources/application.properties`

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/municipal_reservations
spring.datasource.username=admin_user
spring.datasource.password=admin123

# JWT
jwt.secret=TU_SECRET_KEY_SUPER_SECRETA_AQUI
jwt.expiration=86400000

# Azure AD
azure.ad.tenant-id=TU_TENANT_ID
azure.ad.client-id=TU_CLIENT_ID

# OpenWeather
openweather.api.key=TU_OPENWEATHER_API_KEY
openweather.api.base-url=https://api.openweathermap.org/data/2.5

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
```

#### 9.2.4 Compilar y Ejecutar
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### 9.3 Instalación Frontend (JavaFX)

#### 9.3.1 Navegar al módulo
```bash
cd municipal-admin-fx
```

#### 9.3.2 Configurar API URL
Editar: `src/main/resources/config/application.properties`

```properties
api.base.url=http://localhost:8080/api
```

#### 9.3.3 Compilar y Ejecutar
```bash
mvn clean javafx:run
```

### 9.4 Verificación de Instalación

#### 9.4.1 Verificar Backend
```bash
curl http://localhost:8080/actuator/health
```
Respuesta esperada:
```json
{"status":"UP"}
```

#### 9.4.2 Acceder a Swagger
Abrir en navegador: `http://localhost:8080/swagger-ui.html`

#### 9.4.3 Verificar Base de Datos
```bash
docker exec -it projectcodex-db-1 psql -U admin_user -d municipal_reservations -c "\dt"
```

### 9.5 Usuario Administrador Inicial

El sistema crea automáticamente un usuario admin al iniciar:

```
Email: admin@municipal.com
Rol: ADMIN
```
(Autenticación vía Azure AD)

---

## 10. Manual de Usuario

### 10.1 Acceso al Sistema

1. Ejecutar aplicación JavaFX
2. Hacer clic en "Login with Azure AD"
3. Ingresar credenciales corporativas
4. Sistema redirige al dashboard según rol

### 10.2 Dashboard de Usuario (USER)

#### 10.2.1 Crear Reservación
1. Clic en "Nueva Reserva"
2. Seleccionar espacio del catálogo
3. Elegir fecha y hora
4. Especificar número de asistentes
5. Agregar notas (opcional)
6. Clic en "Reservar"
7. Recibir email con código QR

#### 10.2.2 Ver Mis Reservas
- Lista con todas las reservas
- Filtrar por estado
- Ver detalles
- Cancelar reserva (si está PENDING o CONFIRMED)

#### 10.2.3 Exportar Historial
1. Clic en "Exportar a Excel"
2. Archivo se descarga automáticamente
3. Abrir con Excel/LibreOffice

### 10.3 Dashboard de Supervisor (SUPERVISOR)

#### 10.3.1 Aprobar Reservaciones
1. Ver lista de reservas PENDING
2. Revisar detalles (clima, capacidad, etc.)
3. Clic en "Aprobar" o "Rechazar"
4. Usuario recibe notificación por email

#### 10.3.2 Gestionar Espacios
- Crear nuevos espacios
- Editar información
- Activar/Desactivar
- Subir imágenes
- Configurar horarios

#### 10.3.3 Check-In de Reservas
1. Escanear código QR (o ingresar manualmente)
2. Sistema valida reserva
3. Marcar como CHECKED_IN
4. Registrar timestamp

### 10.4 Dashboard de Administrador (ADMIN)

#### 10.4.1 Gestión de Usuarios
- Crear usuarios
- Asignar roles
- Activar/Desactivar
- Ver historial de actividad

#### 10.4.2 Reportes y Estadísticas
1. Ver métricas en tiempo real:
   - Total usuarios activos
   - Espacios más reservados
   - Tasa de ocupación
   - No-shows
2. Exportar reportes a Excel
3. Filtrar por período

#### 10.4.3 Auditoría
- Ver logs de todas las acciones
- Filtrar por usuario, acción, fecha
- Exportar logs

#### 10.4.4 Configuración del Sistema
- Configurar parámetros
- Gestionar notificaciones
- Configurar integraciones

### 10.5 Características Comunes

#### 10.5.1 Búsqueda de Espacios
1. Clic en "Buscar Espacios"
2. Aplicar filtros:
   - Tipo de espacio
   - Capacidad mínima/máxima
   - Ubicación
3. Ver resultados
4. Clic en espacio para detalles

#### 10.5.2 Calificaciones
Después de completar reserva:
1. Recibir notificación para calificar
2. Asignar estrellas (1-5)
3. Escribir comentario
4. Enviar calificación

#### 10.5.3 Notificaciones
- Panel de notificaciones en dashboard
- Marcar como leídas
- Eliminar notificaciones antiguas

---

## 11. Pruebas

### 11.1 Pruebas Unitarias

#### 11.1.1 Tecnologías
- **JUnit 5:** Framework de testing
- **Mockito:** Mocking de dependencias
- **Spring Boot Test:** Contexto de pruebas

#### 11.1.2 Cobertura
> Nota: El proyecto requiere >70% de cobertura según especificaciones.

Formas de ver la cobertura:

- Reporte HTML clásico:
    ```bash
    mvn clean test jacoco:report
    ```
    Ver en: `target/site/jacoco/index.html`

- Cobertura en consola (una línea):
    - Makefile (recomendado): `make coverage`
    - Tarea VS Code: Paleta (⇧⌘P) → Run Task → `coverage`
    - Maven solamente:
        ```bash
        ./mvnw -q -DskipITs -Djacoco.skip=false -Dgpg.skip -T1C test jacoco:report exec:exec
        ```
    Salida esperada (ejemplo):
    ```
    Instrucciones cubiertas: 97.08%
    Líneas cubiertas: 98.28%
    ```

#### 11.1.3 Ejemplos de Pruebas

**UserServiceTest:**
```java
@Test
void testCreateUser_Success() {
    // Given
    UserInputDTO input = new UserInputDTO(/* ... */);
    
    // When
    UserOutputDTO output = userService.create(input);
    
    // Then
    assertNotNull(output.getId());
    assertEquals(input.getEmail(), output.getEmail());
}
```

**ReservationServiceTest:**
```java
@Test
void testCreateReservation_OverlappingTimes_ThrowsException() {
    // Given
    ReservationDTO dto = createOverlappingReservation();
    
    // When & Then
    assertThrows(BusinessRuleException.class, () -> {
        reservationService.create(dto);
    });
}
```

### 11.2 Pruebas de Integración

#### 11.2.1 Base de Datos H2
Las pruebas de integración usan H2 in-memory:

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### 11.2.2 Ejemplos

**ReservationControllerIntegrationTest:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerIntegrationTest {
    
    @Test
    void testCreateReservation_Returns201() throws Exception {
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

### 11.3 Pruebas Manuales

#### 11.3.1 Checklist de Funcionalidades

**Autenticación:**
- [ ] Login con Azure AD exitoso
- [ ] JWT generado correctamente
- [ ] Token expira después de tiempo configurado
- [ ] Refresh token funciona

**Reservaciones:**
- [ ] Crear reserva genera QR
- [ ] Email enviado con QR embebido
- [ ] Validación de horarios funciona
- [ ] Check-in con QR exitoso
- [ ] Cancelación actualiza estado

**Exportación:**
- [ ] Excel de reservas se descarga
- [ ] Formato correcto (.xlsx)
- [ ] Datos completos y formateados
- [ ] Estadísticas generadas correctamente

**Métricas:**
- [ ] Tasa de ocupación se calcula bien
- [ ] Top espacios ordenados correctamente
- [ ] Distribución horaria precisa

**Búsqueda:**
- [ ] Filtro por tipo funciona
- [ ] Filtro por capacidad preciso
- [ ] Búsqueda por ubicación (parcial)
- [ ] Combinación de filtros

### 11.4 Herramientas de Testing

- **Postman:** Colección de endpoints
- **JMeter:** Pruebas de carga
- **SonarQube:** Análisis de código

---

## 12. Limitaciones y Trabajo Futuro

### 12.1 Limitaciones Conocidas

1. **Autenticación:**
   - Dependencia total de Azure AD
   - No hay autenticación local fallback

2. **Notificaciones:**
   - Solo por email
   - No push notifications en JavaFX

3. **Reportes:**
   - No hay filtros por fecha en Excel export
   - PDF no implementado

4. **Búsqueda:**
   - Sin búsqueda full-text
   - No geolocalización

5. **Escalabilidad:**
   - No hay sistema de colas
   - Sin balanceo de carga

### 12.2 Trabajo Futuro

#### 12.2.1 Corto Plazo
- [ ] Notificaciones push en tiempo real (WebSocket)
- [ ] Exportación a PDF con gráficos
- [ ] Filtros de fecha en reportes
- [ ] Dashboard con gráficas interactivas
- [ ] Modo oscuro en JavaFX

#### 12.2.2 Mediano Plazo
- [ ] App móvil (Android/iOS)
- [ ] Geolocalización de espacios
- [ ] Sistema de pagos integrado
- [ ] Reservas recurrentes
- [ ] Multi-tenancy

#### 12.2.3 Largo Plazo
- [ ] Machine Learning para predicción de demanda
- [ ] Recomendación inteligente de espacios
- [ ] Integración con calendarios (Google, Outlook)
- [ ] API pública para terceros
- [ ] Análisis avanzado de datos

### 12.3 Mejoras de Rendimiento

- [ ] Caché distribuida (Redis)
- [ ] Índices de base de datos optimizados
- [ ] Lazy loading de imágenes
- [ ] Compresión de respuestas HTTP
- [ ] CDN para recursos estáticos

---

## 📞 Soporte y Contacto

**Desarrollador:** [Nombre]  
**Email:** [email@universidad.cr]  
**Repositorio:** https://github.com/usuario/projectcodex  
**Documentación API:** http://localhost:8080/swagger-ui.html  

---

## 📄 Licencia

Este proyecto es parte del curso EIF206 - Programación III de la Universidad Nacional de Costa Rica.

**Fecha de Entrega:** Noviembre 3, 2025  
**Versión:** 1.0.0
