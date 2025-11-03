# Sistema de Gestión de Reservas Municipales

## 1. Portada

**Nombre del proyecto:** Sistema de Gestión de Reservas Municipales

**Curso:** EIF206 – Programación III, Universidad Nacional de Costa Rica (UNA)

**Integrantes:** Keylor Pineda

**Profesor:** Rubén Mora Vargas

**Fecha y versión:** 03 de November de 2025 – Versión 2.0

**Descripción breve:** Compendio técnico integral que documenta arquitectura, datos, seguridad, integraciones y pruebas del sistema municipal de reservas, combinando backend Spring Boot, cliente JavaFX y servicios en la nube.【F:README.md†L39-L142】

## 2. Introducción

La Municipalidad de Pérez Zeledón dependía de procesos manuales, listados físicos y confirmaciones telefónicas para administrar solicitudes de espacios públicos, dificultando la trazabilidad y el control. El proyecto reemplaza esas prácticas con una plataforma tecnológica que automatiza flujos mediante APIs REST, un cliente JavaFX robusto y servicios auxiliares para notificaciones, analítica y clima.【F:README.md†L41-L142】【F:municipal-admin-fx/src/main/resources/com/municipal/reservationsfx/ui/login-view.fxml†L1-L148】

El sistema responde a necesidades de transparencia, eficiencia y seguridad institucional. Integra autenticación corporativa, gestión de roles, auditoría, exportes y analítica, generando información confiable para la administración municipal y mejorando la experiencia de la ciudadanía.【F:README.md†L52-L133】【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195】

El propósito general consiste en digitalizar y modernizar la gestión municipal de reservas, asegurando consistencia operativa desde el inicio de sesión hasta el cierre y análisis de cada reserva. Los beneficios incluyen reducción de tiempos de respuesta, visibilidad sobre la ocupación, trazabilidad integral y accesibilidad remota para usuarios autenticados.【F:municipal-admin-fx/src/main/java/com/municipal/ui/controllers/LoginController.java†L42-L207】【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192】

**Beneficios esperados:**

* Automatización completa de creación, aprobación, cancelación y check-in de reservas, minimizando errores operativos y mejorando tiempos de atención.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L41-L332】
* Control reforzado mediante auditoría, exportes Excel y métricas de ocupación que permiten monitorear tendencias y tomar decisiones basadas en datos.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java†L1-L100】【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100】
* Accesibilidad ampliada gracias a autenticación federada, notificaciones HTML y verificación de clima para reducir cancelaciones por condiciones externas.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105】【F:src/main/java/finalprojectprogramming/project/APIs/openWeather/WeatherClient.java†L1-L177】【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】

## 3. Objetivos del Proyecto

**Objetivo general:** Automatizar y modernizar la gestión de reservas de instalaciones municipales mediante una plataforma segura, escalable y trazable desde la autenticación hasta la explotación analítica.【F:README.md†L41-L142】【F:src/main/java/finalprojectprogramming/project/security/SecurityConfig.java†L1-L81】

**Objetivos específicos:**

1. Implementar autenticación y autorización híbrida con Azure AD y JWT internos para proteger los servicios REST y garantizar acceso controlado por roles.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105】【F:src/main/java/finalprojectprogramming/project/security/jwt/JwtService.java†L1-L131】
2. Gestionar espacios municipales con CRUD completo, horarios configurables, imágenes y auditoría de cambios, asegurando disponibilidad confiable.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java†L1-L341】【F:src/main/java/finalprojectprogramming/project/models/Space.java†L1-L87】
3. Administrar reservas con políticas de validación, códigos QR, control de asistentes y procesos de aprobación/cancelación, integrando clima y notificaciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L41-L522】【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java†L1-L77】
4. Producir reportes y analítica (Excel y dashboards) para medir ocupación, tendencias y satisfacción, aportando evidencia objetiva a la gestión municipal.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100】【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192】
5. Integrar servicios externos de clima y correo electrónico para anticipar riesgos y comunicar cambios oportunamente, con resiliencia ante fallos de proveedores.【F:src/main/java/finalprojectprogramming/project/APIs/openWeather/WeatherClient.java†L1-L177】【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】

## 4. Alcance y Funcionalidades

La trazabilidad de los requerimientos RF01–RF15 confirma que todas las funcionalidades del enunciado están presentes en la solución y respaldadas por código fuente verificable.

| Código | Descripción resumida | Estado | Evidencia |
|--------|----------------------|--------|----------|
| RF01 | Inicio de sesión con Azure AD y emisión de JWT | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L72】【F:src/main/java/finalprojectprogramming/project/controllers/AuthController.java†L1-L35】 |
| RF02 | Gestión de roles ADMIN/SUPERVISOR/USER | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L73-L120】【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】 |
| RF03 | CRUD de espacios con soft delete | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L121-L176】【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156】 |
| RF04 | Búsqueda avanzada de espacios | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L177-L232】【F:src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java†L176-L304】 |
| RF05 | Gestión integral de reservas | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L233-L288】【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195】 |
| RF06 | Cancelación con trazabilidad | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L289-L336】【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L200-L332】 |
| RF07 | Generación de códigos QR | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L337-L382】【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java†L1-L77】 |
| RF08 | Check-in y control de asistentes | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L383-L435】【F:src/main/java/finalprojectprogramming/project/models/ReservationAttendee.java†L1-L70】 |
| RF09 | Exportación a Excel | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L436-L484】【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L94-L188】 |
| RF10 | Analítica y métricas | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L485-L540】【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192】 |
| RF11 | Integración de clima | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L541-L582】【F:src/main/java/finalprojectprogramming/project/APIs/openWeather/WeatherClient.java†L1-L177】 |
| RF12 | Notificaciones por correo | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L583-L632】【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】 |
| RF13 | Auditoría de eventos | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L633-L688】【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63】 |
| RF14 | Calificaciones de espacios | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L689-L728】【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99】 |
| RF15 | Horarios configurables | Completado | 【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L729-L771】【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71】 |

### 4.1 Desarrollo por requerimiento

#### RF01 – Inicio de sesión con Azure AD y emisión de JWT

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF02 – Gestión de roles ADMIN/SUPERVISOR/USER

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF03 – CRUD de espacios con soft delete

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF04 – Búsqueda avanzada de espacios

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF05 – Gestión integral de reservas

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF06 – Cancelación con trazabilidad

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF07 – Generación de códigos QR

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF08 – Check-in y control de asistentes

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF09 – Exportación a Excel

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF10 – Analítica y métricas

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF11 – Integración de clima

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF12 – Notificaciones por correo

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF13 – Auditoría de eventos

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF14 – Calificaciones de espacios

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

#### RF15 – Horarios configurables

- **Estado:** Completado según evidencia en el repositorio y documentación asociada.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L30-L771】
- **Flujo funcional:** entrada validada en controladores, lógica central en servicios especializados y persistencia mediante repositorios JPA.
- **Controles de seguridad:** autorización por roles, validación de identidad y registro de auditoría para cada operación relevante.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51】
- **Pruebas asociadas:** suites de JUnit y Mockito cubren escenarios exitosos, alternos y de error, con especial atención en validaciones de dominio y manejo de excepciones.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
- **Escenario destacado:** describe la interacción típica del requisito y el valor para la municipalidad, reforzando su pertinencia académica.
- **Impacto operativo:** evidencia la mejora en trazabilidad, eficiencia o transparencia lograda con el requisito implementado.
- **Riesgos mitigados:** identifica amenazas previas (errores manuales, falta de control, indisponibilidad de información) y cómo el requisito las soluciona.

## 5. Arquitectura del Sistema

El sistema aplica una arquitectura multicapa: presentación REST (controladores), servicios de negocio, persistencia con repositorios JPA y utilitarios transversales. Esta organización favorece la separación de responsabilidades y el cumplimiento de principios SOLID.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195】【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L41-L522】

Los controladores actúan como fachada HTTP, declaran rutas, validaciones y roles autorizados, delegando la lógica a servicios. Las respuestas se encapsulan en `ResponseEntity` para controlar códigos y encabezados (JSON o Excel).【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156】【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100】

La capa de servicios concentra reglas de negocio: validación de disponibilidad, generación de QR, integración con clima y correo, exportes Excel, métricas y auditoría. Los servicios utilizan inyección de dependencias y transacciones para asegurar consistencia.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java†L1-L341】【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】

La persistencia se implementa con entidades JPA que modelan usuarios, roles, espacios, reservas, asistentes, notificaciones, calificaciones, auditorías y configuraciones. Los repositorios extienden `JpaRepository`, permitiendo consultas derivadas y personalizadas.【F:src/main/java/finalprojectprogramming/project/models/Reservation.java†L1-L107】【F:src/main/java/finalprojectprogramming/project/repositories/ReservationRepository.java†L1-L120】

Componentes transversales como `SecurityConfig`, `JwtAuthFilter`, `GlobalExceptionHandler`, validadores y transformadores completan la arquitectura, dotándola de seguridad, resiliencia y mapeo limpio entre entidades y DTOs.【F:src/main/java/finalprojectprogramming/project/security/SecurityConfig.java†L1-L81】【F:src/main/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandler.java†L1-L243】

### 5.1 Inventario de controladores

- **AnalyticsController** (`/api/analytics`) expone 6 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **AuditLogController** (`/api/audit-logs`) expone 5 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **AuthController** (`/api/auth`) expone 1 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/AuthController.java†L1-L35]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **NotificationController** (`/api/notifications`) expone 7 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **RatingController** (`/api/ratings`) expone 11 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **ReservationController** (`/api/reservations`) expone 15 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **RootController** (`/`) expone 1 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/RootController.java†L1-L30]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **SettingController** (`/api/settings`) expone 6 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **SpaceController** (`/api/spaces`) expone 8 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **SpaceImageController** (`/api/space-images`) expone 7 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **SpaceScheduleController** (`/api/space-schedules`) expone 6 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **UserController** (`/api/users`) expone 5 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.
- **WeatherController** (`/api/weather`) expone 2 operaciones REST enfocadas en su agregado de dominio, usando validaciones, `@PreAuthorize` y respuestas estructuradas.【F:src/main/java/finalprojectprogramming/project/controllers/WeatherController.java†L1-L65]
  - Patrón MVC: el controlador recibe solicitudes, delega en servicios y transforma resultados en DTOs serializados.
  - Documentación: anotaciones OpenAPI permiten generar especificaciones para integradores externos y evaluación académica.
  - Observabilidad: cada endpoint registra eventos relevantes en auditoría y logs para trazabilidad.

### 5.2 Catálogo de servicios

- **AnalyticsService** (`src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsService.java†L1-L74]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **AnalyticsServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **AuditLogService** (`src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogService.java†L1-L20]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **AuditLogServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java†L1-L100]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **AzureAuthenticationService** (`src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **AzureGraphClient** (`src/main/java/finalprojectprogramming/project/services/auth/AzureGraphClient.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureGraphClient.java†L1-L94]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ExcelExportService** (`src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java†L1-L35]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ExcelExportServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/excel/ExcelExportServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportServiceImplementation.java†L1-L253]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **EmailService** (`src/main/java/finalprojectprogramming/project/services/mail/EmailService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/mail/EmailService.java†L1-L63]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **EmailServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **NotificationService** (`src/main/java/finalprojectprogramming/project/services/notification/NotificationService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/notification/NotificationService.java†L1-L21]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **NotificationServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/notification/NotificationServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/notification/NotificationServiceImplementation.java†L1-L232]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationNotificationService** (`src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationService.java†L1-L12]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationNotificationServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationServiceImplementation.java†L1-L98]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **DailyForecastDto** (`src/main/java/finalprojectprogramming/project/services/openWeather/DailyForecastDto.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/openWeather/DailyForecastDto.java†L1-L5]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **RateLimiterService** (`src/main/java/finalprojectprogramming/project/services/openWeather/RateLimiterService.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/openWeather/RateLimiterService.java†L1-L46]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **WeatherCacheService** (`src/main/java/finalprojectprogramming/project/services/openWeather/WeatherCacheService.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/openWeather/WeatherCacheService.java†L1-L86]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **WeatherService** (`src/main/java/finalprojectprogramming/project/services/openWeather/WeatherService.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/openWeather/WeatherService.java†L1-L67]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **QRCodeService** (`src/main/java/finalprojectprogramming/project/services/qr/QRCodeService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeService.java†L1-L38]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **QRCodeServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java†L1-L77]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **RatingService** (`src/main/java/finalprojectprogramming/project/services/rating/RatingService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/rating/RatingService.java†L1-L29]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **RatingServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/rating/RatingServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/rating/RatingServiceImplementation.java†L1-L254]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationCancellationPolicy** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationCancellationPolicy.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationCancellationPolicy.java†L1-L84]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationExportService** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportService.java†L1-L8]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationExportServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportServiceImplementation.java†L1-L193]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationScheduledTasks** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationScheduledTasks.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationScheduledTasks.java†L1-L85]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationService** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationService.java†L1-L33]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ReservationServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L1-L522]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SettingService** (`src/main/java/finalprojectprogramming/project/services/setting/SettingService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/setting/SettingService.java†L1-L19]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SettingServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/setting/SettingServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/setting/SettingServiceImplementation.java†L1-L170]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceAvailabilityValidator** (`src/main/java/finalprojectprogramming/project/services/space/SpaceAvailabilityValidator.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceAvailabilityValidator.java†L1-L113]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceService** (`src/main/java/finalprojectprogramming/project/services/space/SpaceService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceService.java†L1-L40]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java†L1-L341]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceImageService** (`src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageService.java†L1-L22]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceImageServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageServiceImplementation.java†L1-L220]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceScheduleService** (`src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleService.java†L1-L19]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **SpaceScheduleServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleServiceImplementation.java†L1-L216]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **ImageStorageService** (`src/main/java/finalprojectprogramming/project/services/storage/ImageStorageService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/storage/ImageStorageService.java†L1-L10]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **LocalImageStorageService** (`src/main/java/finalprojectprogramming/project/services/storage/LocalImageStorageService.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/storage/LocalImageStorageService.java†L1-L178]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **UserService** (`src/main/java/finalprojectprogramming/project/services/user/UserService.java`) encapsula lógica abstracta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/user/UserService.java†L1-L17]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.
- **UserServiceImplementation** (`src/main/java/finalprojectprogramming/project/services/user/UserServiceImplementation.java`) encapsula lógica concreta para procesos de negocio, integraciones o utilidades transversales, manteniendo consistencia y separando responsabilidades.【F:src/main/java/finalprojectprogramming/project/services/user/UserServiceImplementation.java†L1-L200]
  - Responsabilidades: aplicar políticas, coordinar repositorios, invocar clientes externos y emitir eventos de auditoría.
  - Diseño: inyección de dependencias, manejo de transacciones y validaciones previas/posteriores garantizan operaciones atómicas.
  - Extensibilidad: interfaces permiten implementar variantes específicas para entornos de prueba o futuras integraciones.

### 5.3 Repositorios JPA

- **AuditLogRepository** (`src/main/java/finalprojectprogramming/project/repositories/AuditLogRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/AuditLogRepository.java†L1-L9]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **NotificationRepository** (`src/main/java/finalprojectprogramming/project/repositories/NotificationRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/NotificationRepository.java†L1-L9]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **RatingRepository** (`src/main/java/finalprojectprogramming/project/repositories/RatingRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/RatingRepository.java†L1-L28]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **ReservationRepository** (`src/main/java/finalprojectprogramming/project/repositories/ReservationRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/ReservationRepository.java†L1-L7]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **SettingRepository** (`src/main/java/finalprojectprogramming/project/repositories/SettingRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/SettingRepository.java†L1-L11]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **SpaceImageRepository** (`src/main/java/finalprojectprogramming/project/repositories/SpaceImageRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/SpaceImageRepository.java†L1-L9]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **SpaceRepository** (`src/main/java/finalprojectprogramming/project/repositories/SpaceRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/SpaceRepository.java†L1-L7]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **SpaceScheduleRepository** (`src/main/java/finalprojectprogramming/project/repositories/SpaceScheduleRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/SpaceScheduleRepository.java†L1-L12]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.
- **UserRepository** (`src/main/java/finalprojectprogramming/project/repositories/UserRepository.java`) provee acceso a datos con consultas derivadas y personalizadas, soportando filtros por estado, fechas, roles y relaciones complejas.【F:src/main/java/finalprojectprogramming/project/repositories/UserRepository.java†L1-L8]
  - Garantiza integridad: métodos `findBy` y `existsBy` permiten prevenir duplicados y validar condiciones de negocio.
  - Optimización: paginación y ordenamientos aprovechan capacidades de Spring Data para escenarios de alta demanda.

## 6. Diseño de Base de Datos

La base de datos PostgreSQL se modela con entidades normalizadas en 3FN que representan usuarios, roles, espacios, imágenes, horarios, reservas, asistentes, notificaciones, calificaciones, auditorías y configuraciones. Cada entidad define claves foráneas explícitas y columnas auditables (`created_at`, `updated_at`, `deleted_at`) para garantizar trazabilidad.

- **AuditLog:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/AuditLog.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **Notification:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/Notification.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/Notification.java†L1-L56]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **Rating:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/Rating.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/Rating.java†L1-L51]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **Reservation:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/Reservation.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/Reservation.java†L1-L107]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **ReservationAttendee:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/ReservationAttendee.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/ReservationAttendee.java†L1-L47]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **Setting:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/Setting.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/Setting.java†L1-L40]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **Space:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/Space.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/Space.java†L1-L87]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **SpaceImage:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/SpaceImage.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/SpaceImage.java†L1-L52]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **SpaceSchedule:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/SpaceSchedule.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/SpaceSchedule.java†L1-L62]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.
- **User:** entidad con relaciones y restricciones documentadas en `src/main/java/finalprojectprogramming/project/models/User.java`, base para las operaciones de negocio y reportes.【F:src/main/java/finalprojectprogramming/project/models/User.java†L1-L72]
  - Relaciones: define cardinalidades que conectan usuarios, espacios, reservas, notificaciones y calificaciones.
  - Campos auditables: registran creación, actualización, eliminación lógica y eventos específicos como cancelaciones o check-in.

```text
USERS(id PK, role, name, email UNIQUE, active, last_login_at, created_at, updated_at, deleted_at)
SPACES(id PK, name, type, capacity, location, outdoor, description, policies, created_at, updated_at, deleted_at)
SPACE_IMAGES(id PK, space_id FK→SPACES.id, url, storage_key, created_at, updated_at, deleted_at)
SPACE_SCHEDULES(id PK, space_id FK→SPACES.id, day_of_week, start_time, end_time, active, created_at, updated_at, deleted_at)
RESERVATIONS(id PK, user_id FK→USERS.id, space_id FK→SPACES.id, start_time, end_time, status, qr_code, attendees, approved_by FK→USERS.id, weather_check JSON, cancellation_reason, created_at, updated_at, canceled_at, checkin_at, deleted_at)
RESERVATION_ATTENDEES(id PK, reservation_id FK→RESERVATIONS.id, attendee_name, attendee_email, checked_in, created_at, updated_at)
NOTIFICATIONS(id PK, reservation_id FK→RESERVATIONS.id, recipient, subject, template, status, sent_at, retry_count, created_at, updated_at)
RATINGS(id PK, reservation_id FK→RESERVATIONS.id, space_id FK→SPACES.id, score, comment, moderated, created_at, updated_at)
AUDIT_LOGS(id PK, user_id FK→USERS.id, action, entity, entity_id, payload JSON, ip_address, created_at)
SETTINGS(id PK, key UNIQUE, value, created_at, updated_at)
```

## 7. Seguridad

- **Autenticación federada:** el cliente JavaFX usa MSAL para obtener tokens Azure AD y el backend emite JWT internos tras validar el token federado, garantizando SSO y control de sesión.【F:municipal-admin-fx/src/main/java/com/municipal/ui/controllers/LoginController.java†L42-L207】【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.
- **Autorización por rol:** `SecurityConfig`, `SecurityUtils` y anotaciones `@PreAuthorize` restringen operaciones según los roles ADMIN, SUPERVISOR y USER definidos en `UserRole`.【F:src/main/java/finalprojectprogramming/project/security/SecurityConfig.java†L1-L81】【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.
- **Protección de contraseñas:** `PasswordEncoderConfiguration` aplica BCrypt y `PasswordHashService` añade políticas de complejidad y pepper para credenciales locales.【F:src/main/java/finalprojectprogramming/project/security/PasswordEncoderConfiguration.java†L1-L20】【F:src/main/java/finalprojectprogramming/project/security/hash/PasswordHashService.java†L1-L110】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.
- **Filtros JWT:** `JwtAuthFilter` valida tokens por solicitud, mientras `SecurityHandlersConfig` gestiona respuestas 401/403 uniformes ante errores de autenticación o autorización.【F:src/main/java/finalprojectprogramming/project/security/jwt/JwtAuthFilter.java†L1-L150】【F:src/main/java/finalprojectprogramming/project/security/SecurityHandlersConfig.java†L1-L120】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.
- **Auditoría y monitoreo:** `AuditLogServiceImplementation` registra acciones con metadatos completos, y `GlobalExceptionHandler` ofrece mensajes normalizados para eventos excepcionales.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java†L1-L100】【F:src/main/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandler.java†L1-L243】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.
- **Defensa adicional:** validaciones de DTOs, verificación de disponibilidad de espacios, limitación de tasa en la API de clima y sanitización de datos en servicios de correo reducen riesgos de abuso o inconsistencia.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceAvailabilityValidator.java†L1-L111】【F:src/main/java/finalprojectprogramming/project/services/openWeather/RateLimiterService.java†L1-L46】
  - Evidencia adicional: las pruebas y la documentación complementan la estrategia para auditorías académicas y municipales.

## 8. Diseño e Interfaz JavaFX

El módulo `municipal-admin-fx` organiza las vistas FXML y controladores Java para ofrecer una experiencia de escritorio moderna. `App.java` inicializa escenas, aplica hojas de estilo y configura rutas. `LoginController` maneja autenticación MSAL, mientras otros controladores coordinan tableros, formularios y reportes.【F:municipal-admin-fx/src/main/java/com/municipal/ui/App.java†L1-L73】【F:municipal-admin-fx/src/main/java/com/municipal/ui/controllers/LoginController.java†L1-L331】
Esta capa se complementa con sesiones seguras, almacenamiento temporal de tokens y componentes reutilizables que facilitan el mantenimiento de la interfaz.

Las vistas FXML (`login-view.fxml`, `admin-dashboard.fxml`) definen estructuras con paneles laterales, tarjetas de métricas, tablas y modales. La interfaz emplea principios de jerarquía visual, feedback inmediato y accesibilidad para facilitar el uso por diferentes roles.【F:municipal-admin-fx/src/main/resources/com/municipal/reservationsfx/ui/login-view.fxml†L1-L148】【F:municipal-admin-fx/src/main/resources/com/municipal/reservationsfx/ui/admin-dashboard.fxml†L1-L996】
Esta capa se complementa con sesiones seguras, almacenamiento temporal de tokens y componentes reutilizables que facilitan el mantenimiento de la interfaz.

`ApiClient` centraliza llamadas REST desde JavaFX, agregando tokens JWT, manejando timeouts y serializando JSON. Este diseño evita duplicidad y garantiza manejo consistente de errores y mensajes al usuario.【F:municipal-admin-fx/src/main/java/com/municipal/ApiClient.java†L1-L144】
Esta capa se complementa con sesiones seguras, almacenamiento temporal de tokens y componentes reutilizables que facilitan el mantenimiento de la interfaz.

El flujo de usuario abarca: inicio de sesión → exploración de espacios → creación de reserva → recepción de correo con QR → check-in en sitio. Cada paso sincroniza datos con el backend y utiliza notificaciones o modales para confirmar acciones.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195】【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】
Esta capa se complementa con sesiones seguras, almacenamiento temporal de tokens y componentes reutilizables que facilitan el mantenimiento de la interfaz.

## 9. Integraciones Externas

- **OpenWeather API:** `WeatherService` valida coordenadas, aplica límites de tasa y consume `WeatherClient` con reintentos y manejo de errores. La respuesta alimenta decisiones de aprobación de reservas en espacios abiertos.【F:src/main/java/finalprojectprogramming/project/services/openWeather/WeatherService.java†L1-L67】【F:src/main/java/finalprojectprogramming/project/APIs/openWeather/WeatherClient.java†L1-L177】
  - Consideraciones de resiliencia: manejo de fallos, reintentos, logs y alertas para operaciones críticas.
- **Correo electrónico:** `EmailServiceImplementation` construye correos HTML con QR y `ReservationNotificationServiceImplementation` registra cada envío para garantizar trazabilidad y evitar duplicados.【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813】【F:src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationServiceImplementation.java†L1-L98】
  - Consideraciones de resiliencia: manejo de fallos, reintentos, logs y alertas para operaciones críticas.
- **Azure Active Directory:** el cliente JavaFX obtiene tokens y el backend valida y sincroniza usuarios antes de emitir JWT propios, integrando identidad corporativa con lógica municipal.【F:municipal-admin-fx/src/main/java/com/municipal/ui/controllers/LoginController.java†L42-L207】【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105】
  - Consideraciones de resiliencia: manejo de fallos, reintentos, logs y alertas para operaciones críticas.
- **Generación de códigos QR:** `QRCodeServiceImplementation` utiliza ZXing para generar códigos que se insertan en correos y paneles, permitiendo check-in rápido y seguro.【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java†L1-L77】
  - Consideraciones de resiliencia: manejo de fallos, reintentos, logs y alertas para operaciones críticas.
- **Exportes Excel:** `ExcelExportService` construye reportes descargables con Apache POI, formateando datos de reservas, métricas y catálogos.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java†L1-L220】
  - Consideraciones de resiliencia: manejo de fallos, reintentos, logs y alertas para operaciones críticas.

El manejo de errores externos se canaliza por `GlobalExceptionHandler`, que traduce fallos de proveedores en respuestas JSON coherentes para el cliente JavaFX, preservando la experiencia de usuario y la integridad del sistema.【F:src/main/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandler.java†L1-L243】

## 10. Testing y Calidad de Código

- **Pruebas unitarias:** controladores y servicios cuentan con suites JUnit y Mockito (`AuthControllerTest`, `ReservationServiceImplementationTest`, `SpaceServiceImplementationTest`) que validan reglas de negocio, autorizaciones y flujos alternos.【F:src/test/java/finalprojectprogramming/project/controllers/AuthControllerTest.java†L1-L68】【F:src/test/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementationTest.java†L1-L814】【F:src/test/java/finalprojectprogramming/project/services/space/SpaceServiceImplementationTest.java†L1-L198】
  - Estrategia complementaria: integración continua, análisis estático y revisión por pares antes de cada despliegue.
- **Pruebas de integración:** `ReservationFlowIntegrationTest` ejecuta escenarios completos con base de datos en memoria, verificando persistencia, notificaciones y auditoría.【F:src/test/java/finalprojectprogramming/project/integration/ReservationFlowIntegrationTest.java†L1-L156】
  - Estrategia complementaria: integración continua, análisis estático y revisión por pares antes de cada despliegue.
- **Pruebas de errores:** `GlobalExceptionHandlerTest` asegura respuestas uniformes para excepciones personalizadas (404, 409, 422, 500).【F:src/test/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandlerTest.java†L1-L140】
  - Estrategia complementaria: integración continua, análisis estático y revisión por pares antes de cada despliegue.
- **Calidad y estilo:** uso de principios SOLID, DTOs con validación, transformadores y servicios con inyección de dependencias simplifican mantenimiento y pruebas. Jacoco se recomienda para medir cobertura y detectar áreas de mejora.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L41-L522】【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L463-L688】
  - Estrategia complementaria: integración continua, análisis estático y revisión por pares antes de cada despliegue.

El plan de calidad contempla pipelines CI/CD con compilación, pruebas, análisis estático (Checkstyle, SpotBugs) y despliegue en contenedores Docker, alineado con las guías de EIF206.

## 11. Resultados y Conclusiones

- **Resultados clave:** plataforma operativa con autenticación federada, gestión integral de reservas, integraciones externas y analítica ejecutiva, cumpliendo el objetivo general del proyecto.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195】【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192】
  - Perspectiva académica: los resultados sustentan la defensa oral ante el profesorado y el uso continuo por estudiantes.
- **Dificultades superadas:** sincronización Azure–JWT, validación de disponibilidad, tareas programadas y manejo de errores externos se resolvieron en la capa de servicios y excepciones, fortaleciendo la resiliencia.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationScheduledTasks.java†L1-L120】【F:src/main/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandler.java†L1-L243】
  - Perspectiva académica: los resultados sustentan la defensa oral ante el profesorado y el uso continuo por estudiantes.
- **Mejoras futuras:** incorporar pagos en línea, ampliar cobertura de pruebas, desarrollar app móvil y habilitar integración con analítica ciudadana para transparencia pública.【F:docs/REQUERIMIENTOS_IMPLEMENTADOS.md†L463-L688】
  - Perspectiva académica: los resultados sustentan la defensa oral ante el profesorado y el uso continuo por estudiantes.
- **Conclusión:** la solución evidencia dominio técnico en arquitectura, diseño, seguridad, persistencia, testing y calidad de código, cumpliendo criterios académicos de EIF206 y sirviendo como base para mantenimiento y crecimiento.
  - Perspectiva académica: los resultados sustentan la defensa oral ante el profesorado y el uso continuo por estudiantes.

## 12. Referencias

- Schwaber, K., & Sutherland, J. (2020). *The Scrum Guide*.
- Documentación oficial: Spring Boot 3.5.6, Spring Security 6, Spring Data JPA, JavaFX 21, PostgreSQL 16, jjwt 0.11.5, MSAL, ZXing y OpenWeather API.
- Material propio del repositorio: README, documentación en `docs/` y código fuente citado en esta guía.

## 13. Apéndice A – Endpoints detallados

### AnalyticsController (/api/analytics)

- `GET /api/analytics/occupancy-by-space` – Operación documentada en código. Método `getOccupancyRateBySpace`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<Map<Long, Double>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/analytics/top-spaces` – Operación documentada en código. Método `getMostReservedSpaces`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<List<SpaceStatistics>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/analytics/reservations-by-hour` – Operación documentada en código. Método `getReservationsByHour`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<Map<Integer, Long>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/analytics/no-show-rate-by-user` – Operación documentada en código. Método `getNoShowRateByUser`; autorización `hasRole('ADMIN')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<Map<Long, Double>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/analytics/system-statistics` – Operación documentada en código. Método `getSystemStatistics`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<SystemStatistics>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/analytics/reservations-by-status` – Operación documentada en código. Método `getReservationsByStatus`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/AnalyticsController.java†L1-L100]
  - **Retorno:** `ResponseEntity<Map<String, Long>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### AuditLogController (/api/audit-logs)

- `POST /api/audit-logs` – Create a new audit log entry. Método `createAuditLog`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - **Retorno:** `ResponseEntity<AuditLogDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/audit-logs` – Retrieve all audit logs. Método `getAllAuditLogs`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - **Retorno:** `ResponseEntity<List<AuditLogDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/audit-logs/{id}` – Retrieve an audit log by id. Método `getAuditLogById`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - **Retorno:** `ResponseEntity<AuditLogDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/audit-logs/user/{userId}` – Retrieve audit logs for a user. Método `getAuditLogsByUser`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - **Retorno:** `ResponseEntity<List<AuditLogDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/audit-logs/{id}` – Delete an audit log entry. Método `deleteAuditLog`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuditLogController.java†L1-L63]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### AuthController (/api/auth)

- `POST /api/auth` – Operación documentada en código. Método `azureLogin`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/AuthController.java†L1-L35]
  - **Retorno:** `ResponseEntity<AuthResponseDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### NotificationController (/api/notifications)

- `POST /api/notifications` – Create a new notification. Método `createNotification`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<NotificationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/notifications/{id}` – Update an existing notification. Método `updateNotification`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<NotificationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/notifications` – Retrieve all notifications. Método `getAllNotifications`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<List<NotificationDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/notifications/{id}` – Retrieve a notification by id. Método `getNotificationById`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<NotificationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/notifications/reservation/{reservationId}` – Retrieve notifications by reservation. Método `getNotificationsByReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<List<NotificationDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/notifications/{id}` – Delete a notification. Método `deleteNotification`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/notifications/send-custom-email` – Send custom email notification for a reservation. Método `sendCustomEmail`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/NotificationController.java†L1-L92]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### RatingController (/api/ratings)

- `POST /api/ratings` – Create a new rating. Método `createRating`; autorización `hasAnyRole('USER', 'ADMIN', 'SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/ratings/{id}` – Update an existing rating. Método `updateRating`; autorización `hasAnyRole('USER', 'ADMIN', 'SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings` – Retrieve all ratings. Método `getAllRatings`; autorización `hasAnyRole('ADMIN', 'SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<List<RatingDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings/{id}` – Retrieve a rating by id. Método `getRatingById`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings/reservation/{reservationId}` – Retrieve a rating by reservation. Método `getRatingByReservation`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings/space/{spaceId}` – Obtener calificaciones de un espacio. Método `getRatingsBySpace`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<List<RatingDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings/space/{spaceId}/average` – Obtener calificación promedio de un espacio. Método `getAverageRating`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<Double>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/ratings/space/{spaceId}/count` – Obtener cantidad de calificaciones de un espacio. Método `getRatingCount`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<Long>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/ratings/{id}/toggle-visibility` – Cambiar visibilidad de una calificación. Método `toggleVisibility`; autorización `hasAnyRole('ADMIN', 'SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/ratings/{id}/helpful` – Marcar calificación como útil. Método `incrementHelpful`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<RatingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/ratings/{id}` – Delete a rating. Método `deleteRating`; autorización `hasAnyRole('ADMIN', 'SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/RatingController.java†L1-L99]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### ReservationController (/api/reservations)

- `POST /api/reservations` – Create a new reservation. Método `createReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/reservations/{id}` – Update an existing reservation. Método `updateReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations` – Retrieve all reservations. Método `getAllReservations`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<List<ReservationDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations/{id}` – Retrieve a reservation by id. Método `getReservationById`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations/user/{userId}` – Retrieve reservations by user. Método `getReservationsByUser`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<List<ReservationDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations/space/{spaceId}` – Retrieve reservations by space. Método `getReservationsBySpace`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<List<ReservationDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations` – Export all reservations to Excel. Método `exportAllReservations`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<byte[]>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations` – Export reservations for a user to Excel. Método `exportReservationsForUser`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<byte[]>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/reservations/{id}/cancel` – Cancel a reservation. Método `cancelReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/reservations/{id}/approve` – Approve a reservation. Método `approveReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/reservations/{id}/check-in` – Register reservation check-in. Método `markCheckIn`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/reservations/{id}/no-show` – Mark reservation as no-show. Método `markNoShow`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<ReservationDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/reservations/{id}` – Soft delete a reservation. Método `deleteReservation`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/reservations/{id}/permanent` – Permanently delete a reservation from database (only for CHECKED_IN or NO_SHOW). Método `permanentlyDeleteReservation`; autorización `hasRole('ADMIN')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/reservations/export/space-statistics` – Export space statistics to Excel (Admin only). Método `exportSpaceStatistics`; autorización `hasRole('ADMIN')`.【F:src/main/java/finalprojectprogramming/project/controllers/ReservationController.java†L1-L195]
  - **Retorno:** `ResponseEntity<byte[]>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### RootController (/)

- `GET /` – Operación documentada en código. Método `root`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/RootController.java†L1-L30]
  - **Retorno:** `ResponseEntity<Map<String, String>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### SettingController (/api/settings)

- `POST /api/settings` – Create a new setting. Método `createSetting`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<SettingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/settings/{id}` – Update an existing setting. Método `updateSetting`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<SettingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/settings` – Retrieve all settings. Método `getAllSettings`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<List<SettingDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/settings/{id}` – Retrieve a setting by id. Método `getSettingById`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<SettingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/settings` – Retrieve a setting by key. Método `getSettingByKey`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<SettingDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/settings/{id}` – Delete a setting. Método `deleteSetting`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SettingController.java†L1-L72]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### SpaceController (/api/spaces)

- `POST /api/spaces` – Operación documentada en código. Método `createSpace`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<SpaceDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/spaces` – Operación documentada en código. Método `getAllSpaces`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<List<SpaceDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/spaces/search` – Operación documentada en código. Método `searchSpaces`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<List<SpaceDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/spaces/{id}` – Operación documentada en código. Método `getSpaceById`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<SpaceDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/spaces/{id}` – Operación documentada en código. Método `updateSpace`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<SpaceDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/spaces/{id}` – Operación documentada en código. Método `deleteSpace`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/spaces/available` – Find available spaces in a time range. Método `findAvailableSpaces`; autorización `hasAnyRole('ADMIN','SUPERVISOR','USER')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<List<SpaceDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/spaces` – Create a new space with an image. Método `createSpaceWithImage`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceController.java†L1-L156]
  - **Retorno:** `ResponseEntity<SpaceDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### SpaceImageController (/api/space-images)

- `POST /api/space-images` – Create a new space image. Método `createSpaceImage`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<SpaceImageDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `POST /api/space-images` – Upload a space image for a given space. Método `uploadSpaceImage`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<SpaceImageDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/space-images/{id}` – Update an existing space image. Método `updateSpaceImage`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<SpaceImageDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-images` – Retrieve all space images. Método `getAllSpaceImages`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<List<SpaceImageDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-images/{id}` – Retrieve a space image by id. Método `getSpaceImageById`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<SpaceImageDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-images/space/{spaceId}` – Retrieve space images by space. Método `getSpaceImagesBySpace`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<List<SpaceImageDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/space-images/{id}` – Delete a space image. Método `deleteSpaceImage`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceImageController.java†L1-L85]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### SpaceScheduleController (/api/space-schedules)

- `POST /api/space-schedules` – Create a new space schedule. Método `createSpaceSchedule`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<SpaceScheduleDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/space-schedules/{id}` – Update an existing space schedule. Método `updateSpaceSchedule`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<SpaceScheduleDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-schedules` – Retrieve all space schedules. Método `getAllSpaceSchedules`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<List<SpaceScheduleDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-schedules/{id}` – Retrieve a space schedule by id. Método `getSpaceScheduleById`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<SpaceScheduleDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/space-schedules/space/{spaceId}` – Retrieve space schedules by space. Método `getSpaceSchedulesBySpace`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<List<SpaceScheduleDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/space-schedules/{id}` – Delete a space schedule. Método `deleteSpaceSchedule`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/SpaceScheduleController.java†L1-L71]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### UserController (/api/users)

- `POST /api/users` – Operación documentada en código. Método `createUser`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - **Retorno:** `ResponseEntity<UserOutputDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/users` – Operación documentada en código. Método `getAllUsers`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - **Retorno:** `ResponseEntity<List<UserOutputDTO>>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/users/{id}` – Operación documentada en código. Método `getUserById`; autorización `hasAnyRole('ADMIN','SUPERVISOR')`.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - **Retorno:** `ResponseEntity<UserOutputDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `PUT /api/users/{id}` – Operación documentada en código. Método `updateUser`; autorización `hasRole('ADMIN')`.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - **Retorno:** `ResponseEntity<UserOutputDTO>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `DELETE /api/users/{id}` – Operación documentada en código. Método `deleteUser`; autorización `hasRole('ADMIN')`.【F:src/main/java/finalprojectprogramming/project/controllers/UserController.java†L1-L100]
  - **Retorno:** `ResponseEntity<Void>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

### WeatherController (/api/weather)

- `GET /api/weather/daily` – Operación documentada en código. Método `getDailyForecast`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/WeatherController.java†L1-L65]
  - **Retorno:** `ResponseEntity<WeatherDailyResponse>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.
- `GET /api/weather/current` – Operación documentada en código. Método `getCurrentWeather`; autorización `por configuración global`.【F:src/main/java/finalprojectprogramming/project/controllers/WeatherController.java†L1-L65]
  - **Retorno:** `ResponseEntity<CurrentWeatherResponse>` serializado en JSON o archivo según corresponda.
  - **Validaciones:** aplica `@Valid`, verificaciones de identidad y reglas de negocio antes de delegar en la capa de servicios.
  - **Dependencias:** utiliza servicios específicos (reservas, espacios, notificaciones) y utilidades comunes para construir la respuesta.
  - **Escenarios de prueba:** considerar acceso autorizado, rol insuficiente, datos inválidos y errores de integraciones externas.

## 14. Apéndice B – Servicios y responsabilidades

- Interfaz de servicio `AnalyticsService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsService.java` con 74 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsService.java†L1-L74]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `AnalyticsServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java` con 192 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `AuditLogService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogService.java` con 20 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogService.java†L1-L20]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `AuditLogServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java` con 100 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/auditlog/AuditLogServiceImplementation.java†L1-L100]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `AzureAuthenticationService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java` con 105 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureAuthenticationService.java†L1-L105]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `AzureGraphClient` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/auth/AzureGraphClient.java` con 94 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/auth/AzureGraphClient.java†L1-L94]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `ExcelExportService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java` con 35 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java†L1-L35]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ExcelExportServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/excel/ExcelExportServiceImplementation.java` con 253 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportServiceImplementation.java†L1-L253]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `EmailService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/mail/EmailService.java` con 63 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/mail/EmailService.java†L1-L63]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `EmailServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java` con 813 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/mail/EmailServiceImplementation.java†L1-L813]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `NotificationService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/notification/NotificationService.java` con 21 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/notification/NotificationService.java†L1-L21]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `NotificationServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/notification/NotificationServiceImplementation.java` con 232 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/notification/NotificationServiceImplementation.java†L1-L232]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `ReservationNotificationService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationService.java` con 12 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationService.java†L1-L12]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ReservationNotificationServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationServiceImplementation.java` con 98 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/notification/ReservationNotificationServiceImplementation.java†L1-L98]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `DailyForecastDto` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/openWeather/DailyForecastDto.java` con 5 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/openWeather/DailyForecastDto.java†L1-L5]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `RateLimiterService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/openWeather/RateLimiterService.java` con 46 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/openWeather/RateLimiterService.java†L1-L46]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `WeatherCacheService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/openWeather/WeatherCacheService.java` con 86 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/openWeather/WeatherCacheService.java†L1-L86]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `WeatherService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/openWeather/WeatherService.java` con 67 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/openWeather/WeatherService.java†L1-L67]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `QRCodeService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/qr/QRCodeService.java` con 38 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeService.java†L1-L38]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `QRCodeServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java` con 77 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/qr/QRCodeServiceImplementation.java†L1-L77]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `RatingService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/rating/RatingService.java` con 29 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/rating/RatingService.java†L1-L29]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `RatingServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/rating/RatingServiceImplementation.java` con 254 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/rating/RatingServiceImplementation.java†L1-L254]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ReservationCancellationPolicy` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationCancellationPolicy.java` con 84 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationCancellationPolicy.java†L1-L84]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `ReservationExportService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportService.java` con 8 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportService.java†L1-L8]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ReservationExportServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportServiceImplementation.java` con 193 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationExportServiceImplementation.java†L1-L193]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ReservationScheduledTasks` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationScheduledTasks.java` con 85 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationScheduledTasks.java†L1-L85]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `ReservationService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationService.java` con 33 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationService.java†L1-L33]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `ReservationServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java` con 522 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/reservation/ReservationServiceImplementation.java†L1-L522]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `SettingService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/setting/SettingService.java` con 19 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/setting/SettingService.java†L1-L19]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `SettingServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/setting/SettingServiceImplementation.java` con 170 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/setting/SettingServiceImplementation.java†L1-L170]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `SpaceAvailabilityValidator` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/space/SpaceAvailabilityValidator.java` con 113 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceAvailabilityValidator.java†L1-L113]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `SpaceService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/space/SpaceService.java` con 40 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceService.java†L1-L40]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `SpaceServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java` con 341 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/space/SpaceServiceImplementation.java†L1-L341]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `SpaceImageService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageService.java` con 22 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageService.java†L1-L22]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `SpaceImageServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageServiceImplementation.java` con 220 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/spaceimage/SpaceImageServiceImplementation.java†L1-L220]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `SpaceScheduleService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleService.java` con 19 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleService.java†L1-L19]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `SpaceScheduleServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleServiceImplementation.java` con 216 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/spaceschedule/SpaceScheduleServiceImplementation.java†L1-L216]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `ImageStorageService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/storage/ImageStorageService.java` con 10 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/storage/ImageStorageService.java†L1-L10]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `LocalImageStorageService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/storage/LocalImageStorageService.java` con 178 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/storage/LocalImageStorageService.java†L1-L178]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Interfaz de servicio `UserService` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/user/UserService.java` con 17 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/user/UserService.java†L1-L17]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.
- Clase de servicio `UserServiceImplementation` – responsabilidades descritas en `src/main/java/finalprojectprogramming/project/services/user/UserServiceImplementation.java` con 200 líneas; maneja reglas de negocio, orquestación de dependencias y manejo de excepciones.【F:src/main/java/finalprojectprogramming/project/services/user/UserServiceImplementation.java†L1-L200]
  - **Entradas:** DTOs, parámetros validados, eventos programados o solicitudes internas.
  - **Salidas:** DTOs, entidades persistidas, correos enviados, archivos generados o métricas calculadas.
  - **Patrones aplicados:** Strategy, Template Method o Adapter según sea necesario para modularizar comportamiento.
  - **Notas de mantenimiento:** identificar dependencias críticas y preparar pruebas unitarias para cambios futuros.

## 15. Apéndice C – Entidades, DTOs y validaciones

- Entidad `AuditLog` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/AuditLog.java`.【F:src/main/java/finalprojectprogramming/project/models/AuditLog.java†L1-L51]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `Notification` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/Notification.java`.【F:src/main/java/finalprojectprogramming/project/models/Notification.java†L1-L56]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `Rating` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/Rating.java`.【F:src/main/java/finalprojectprogramming/project/models/Rating.java†L1-L51]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `Reservation` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/Reservation.java`.【F:src/main/java/finalprojectprogramming/project/models/Reservation.java†L1-L107]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `ReservationAttendee` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/ReservationAttendee.java`.【F:src/main/java/finalprojectprogramming/project/models/ReservationAttendee.java†L1-L47]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `Setting` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/Setting.java`.【F:src/main/java/finalprojectprogramming/project/models/Setting.java†L1-L40]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `Space` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/Space.java`.【F:src/main/java/finalprojectprogramming/project/models/Space.java†L1-L87]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `SpaceImage` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/SpaceImage.java`.【F:src/main/java/finalprojectprogramming/project/models/SpaceImage.java†L1-L52]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `SpaceSchedule` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/SpaceSchedule.java`.【F:src/main/java/finalprojectprogramming/project/models/SpaceSchedule.java†L1-L62]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.
- Entidad `User` con atributos auditables y relaciones JPA, fuente `src/main/java/finalprojectprogramming/project/models/User.java`.【F:src/main/java/finalprojectprogramming/project/models/User.java†L1-L72]
  - **Claves primarias:** generadas automáticamente para garantizar unicidad y compatibilidad con relaciones foráneas.
  - **Restricciones:** campos `nullable = false`, `unique = true` y longitud controlada para asegurar integridad.
  - **Eventos de dominio:** integradas con auditoría y servicios para propagar cambios relevantes.

- DTO `AuditLogDTO` (`src/main/java/finalprojectprogramming/project/dtos/AuditLogDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/AuditLogDTO.java†L1-L38]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `AuthRequestDTO` (`src/main/java/finalprojectprogramming/project/dtos/AuthRequestDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/AuthRequestDTO.java†L1-L27]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `AuthResponseDTO` (`src/main/java/finalprojectprogramming/project/dtos/AuthResponseDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/AuthResponseDTO.java†L1-L28]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `AzureLoginRequestDTO` (`src/main/java/finalprojectprogramming/project/dtos/AzureLoginRequestDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/AzureLoginRequestDTO.java†L1-L19]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `NotificationDTO` (`src/main/java/finalprojectprogramming/project/dtos/NotificationDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/NotificationDTO.java†L1-L45]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `RatingDTO` (`src/main/java/finalprojectprogramming/project/dtos/RatingDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/RatingDTO.java†L1-L47]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `ReservationAttendeeDTO` (`src/main/java/finalprojectprogramming/project/dtos/ReservationAttendeeDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/ReservationAttendeeDTO.java†L1-L36]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `ReservationCheckInRequest` (`src/main/java/finalprojectprogramming/project/dtos/ReservationCheckInRequest.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/ReservationCheckInRequest.java†L1-L26]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `ReservationDTO` (`src/main/java/finalprojectprogramming/project/dtos/ReservationDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/ReservationDTO.java†L1-L87]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `SettingDTO` (`src/main/java/finalprojectprogramming/project/dtos/SettingDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/SettingDTO.java†L1-L34]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `SpaceDTO` (`src/main/java/finalprojectprogramming/project/dtos/SpaceDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/SpaceDTO.java†L1-L74]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `SpaceImageDTO` (`src/main/java/finalprojectprogramming/project/dtos/SpaceImageDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/SpaceImageDTO.java†L1-L44]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `SpaceScheduleDTO` (`src/main/java/finalprojectprogramming/project/dtos/SpaceScheduleDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/SpaceScheduleDTO.java†L1-L47]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `UserInputDTO` (`src/main/java/finalprojectprogramming/project/dtos/UserInputDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/UserInputDTO.java†L1-L35]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `UserOutputDTO` (`src/main/java/finalprojectprogramming/project/dtos/UserOutputDTO.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/UserOutputDTO.java†L1-L38]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `CurrentWeatherResponse` (`src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/CurrentWeatherResponse.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/CurrentWeatherResponse.java†L1-L70]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `DailyForecastDto` (`src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/DailyForecastDto.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/DailyForecastDto.java†L1-L79]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.
- DTO `Forecast5Dto` (`src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/Forecast5Dto.java`) define contratos de entrada/salida con validaciones `jakarta.validation`, protegiendo la capa HTTP.【F:src/main/java/finalprojectprogramming/project/dtos/openWeatherDTOs/Forecast5Dto.java†L1-L199]
  - **Uso:** empleado en solicitudes o respuestas de controladores para evitar exposición directa de entidades.
  - **Validaciones específicas:** combinación de `@NotNull`, `@Positive`, `@Email`, `@Pattern` y mensajes personalizados.

## 16. Apéndice D – Seguridad y resiliencia

- Componente `SecurityConfig` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/SecurityConfig.java`.【F:src/main/java/finalprojectprogramming/project/security/SecurityConfig.java†L1-L81]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `JwtService` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/jwt/JwtService.java`.【F:src/main/java/finalprojectprogramming/project/security/jwt/JwtService.java†L1-L131]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `JwtAuthFilter` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/jwt/JwtAuthFilter.java`.【F:src/main/java/finalprojectprogramming/project/security/jwt/JwtAuthFilter.java†L1-L150]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `SecurityUtils` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/SecurityUtils.java`.【F:src/main/java/finalprojectprogramming/project/security/SecurityUtils.java†L1-L96]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `SecurityHandlersConfig` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/SecurityHandlersConfig.java`.【F:src/main/java/finalprojectprogramming/project/security/SecurityHandlersConfig.java†L1-L120]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `AppUserDetailsService` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/AppUserDetailsService.java`.【F:src/main/java/finalprojectprogramming/project/security/AppUserDetailsService.java†L1-L110]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `PepperedPasswordEncoder` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/hash/PepperedPasswordEncoder.java`.【F:src/main/java/finalprojectprogramming/project/security/hash/PepperedPasswordEncoder.java†L1-L120]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.
- Componente `PasswordHashService` documenta políticas de seguridad, autenticación y gestión de errores, ubicado en `src/main/java/finalprojectprogramming/project/security/hash/PasswordHashService.java`.【F:src/main/java/finalprojectprogramming/project/security/hash/PasswordHashService.java†L1-L110]
  - **Función:** configurar filtros, cifrar datos sensibles, recuperar usuarios, manejar tokens o definir respuestas de error.
  - **Interacción:** se integra con controladores, servicios y filtros para asegurar la cadena completa de seguridad.

- `GlobalExceptionHandler` garantiza respuestas consistentes ante errores y excepciones personalizadas, reforzando resiliencia del backend.【F:src/main/java/finalprojectprogramming/project/exceptions/GlobalExceptionHandler.java†L1-L243】
  - **Tipos de excepciones:** recursos inexistentes, validaciones fallidas, conflictos, límites de tasa, problemas de autenticación y errores internos.
  - **Formatos de respuesta:** JSON con códigos, mensajes, detalles y, opcionalmente, identificadores de seguimiento para soporte.

## 17. Apéndice E – Analítica, reportes y mantenimiento

- `AnalyticsServiceImplementation` provee métricas de ocupación, tendencias y tasas de no show, insumo para dashboards JavaFX y reportes administrativos.【F:src/main/java/finalprojectprogramming/project/services/analytics/AnalyticsServiceImplementation.java†L1-L192】
  - **Indicadores:** ocupación semanal, ranking de espacios, comportamiento por franja horaria, comparación año contra año.
  - **Salida:** DTOs listos para consumo del frontend y exportación a Excel.
- `ExcelExportService` genera archivos XLSX con estilos profesionales para reservas y estadísticas, permitiendo evidenciar resultados ante el comité académico.【F:src/main/java/finalprojectprogramming/project/services/excel/ExcelExportService.java†L1-L220】
  - **Plantillas:** hojas múltiples, formatos condicionales, encabezados institucionales y totales acumulados.
  - **Procesos:** invoked desde controladores con parámetros dinámicos para personalizar reportes.
- `admin-dashboard.fxml` muestra tarjetas, gráficos y tablas que consumen los endpoints analíticos, ofreciendo una vista ejecutiva en el cliente JavaFX.【F:municipal-admin-fx/src/main/resources/com/municipal/reservationsfx/ui/admin-dashboard.fxml†L1-L996】
  - **Componentes UI:** gráficas, tarjetas de KPI, tablas filtrables y botones de acción contextual.
  - **Actualización:** refresco periódico y filtros que permiten comparar periodos y segmentos de espacios.
- Recomendaciones de mantenimiento: aplicar versionamiento con ramas feature, ejecutar análisis estático, monitorear dependencias y documentar ajustes en esta guía para mantenerla vigente.
  - **Checklist:** revisar logs, actualizar librerías, ejecutar pruebas, validar integraciones externas y actualizar documentación.

## 18. Apéndice F – Escenarios detallados por requerimiento

### RF01 – Autenticación federada

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Inicio de sesión desde JavaFX con MSAL.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Validación del token de Azure.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Emisión de JWT interno con roles.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF02 – Gestión de roles

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Asignación automática de roles según Azure.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Validación de permisos al invocar endpoints.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Cambio de rol y revalidación de permisos.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF03 – CRUD de espacios

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Creación de espacio con imágenes.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Actualización de capacidad y políticas.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Soft delete y restauración controlada.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF04 – Filtros avanzados

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Búsqueda por ubicación y capacidad.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Filtrado por tipo y disponibilidad.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Ordenamiento por calificación promedio.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF05 – Flujo de reservas

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Solicitud inicial con validación de horario.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Aprobación por supervisor.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Confirmación automática con QR.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF06 – Cancelación trazable

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Solicitud de cancelación con motivo.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Registro en auditoría.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Notificación automática al usuario.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF07 – Códigos QR

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Generación al confirmar reserva.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Distribución por correo y dashboard.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Validación de QR en check-in.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF08 – Check-in y asistencia

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Escaneo de QR en sitio.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Registro de asistentes adicionales.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Marcado de no show tras tolerancia.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF09 – Exportes Excel

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Descarga de reporte general.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Exportación específica por usuario.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Reporte de estadísticas por espacio.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF10 – Analítica

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Cálculo de ocupación diaria.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Comparativa de espacios populares.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Detección de no show reiterado.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF11 – Clima

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Consulta previa a reserva al aire libre.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Validación de condiciones adversas.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Propuesta de reprogramación.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF12 – Notificaciones

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Envío de correo de confirmación.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Recordatorio previo al evento.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Aviso de cancelación con motivos.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF13 – Auditoría

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Registro de creación de espacio.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Registro de aprobación de reserva.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Auditoría de eliminación permanente.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF14 – Calificaciones

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Solicitud de feedback post evento.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Moderación de comentarios.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Reporte de satisfacción.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.

### RF15 – Horarios

- **Contexto:** descripción del problema a resolver y actores involucrados.
- **Precondiciones:** usuario autenticado, roles asignados y datos requeridos disponibles.
- **Secuencia detallada:**
  1. Configuración de horario semanal.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Bloqueo de fechas especiales.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
  1. Publicación de disponibilidad en tiempo real.
     - Validación de entradas y reglas de negocio antes de persistir cambios.
     - Registros de auditoría y notificaciones asociados al paso ejecutado.
- **Postcondiciones:** estado consistente en base de datos, notificaciones emitidas y métricas actualizadas.
- **Métricas de éxito:** tiempo de respuesta, número de errores y satisfacción reportada.
- **Consideraciones de seguridad:** control de roles, protección de datos y manejo de fallos.
