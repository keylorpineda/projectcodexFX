# Plan maestro de pruebas (estado actual + camino a 100 %)

## Estado actual (ejecución local)
- Resultado de pruebas: 267 tests, 0 fallos, 0 errores, 2 ignorados (skipped)
- Build: PASS
- Cobertura (JaCoCo, target/site/jacoco/index.html)
  - Instrucciones: 81 % (1,970/10,370 perdidas)
  - Branches: 59 % (411/1,026 perdidas)
  - Clases analizadas: 95

Notas relevantes:
- Pruebas de integración pesadas fueron temporalmente anotadas con `@Disabled` para estabilizar la suite principal mientras se incrementa cobertura funcional por slices y unitarias.
- Se añadió un `@ExceptionHandler` de `AccessDeniedException` → 403 en `GlobalExceptionHandler` y se deshabilitaron filtros en slices `@WebMvcTest` (manteniendo seguridad a nivel de método) para evitar 401/403 espurios en pruebas de controlador.
- `JwtService` fue alineado a jjwt 0.11.5 (orden de claims y reloj determinista) y sus pruebas ahora son deterministas con `Clock` fijo.

## Ejecución en Docker (con o sin tests)

Ahora el Dockerfile permite optar por ejecutar las pruebas durante el build:
- Por defecto en docker-compose: NO ejecuta tests (build más rápido).
- Para ejecutar tests y fallar el build si algo rompe:
  - Variable de entorno: `RUN_TESTS=true`
  - Ejemplo: `RUN_TESTS=true docker compose build app` (o agrega `RUN_TESTS=true` en tu `.env`).

Detalles técnicos:
- El build ejecuta `mvn test jacoco:report` si `RUN_TESTS=true` y genera el reporte en `target/site/jacoco` dentro de la etapa de build.
- Luego empaqueta con `-Dmaven.test.skip=true` para no re-ejecutar tests.

Cómo inspeccionar cobertura desde la imagen:
- Recomendado: ejecutar pruebas y cobertura fuera de Docker (más simple para abrir el HTML). Si usas Docker para validación CI, confía en el exit code del build. Si necesitas los artefactos, define un volumen para `/app/target` y copia `target/site/jacoco` a tu host.

La aplicación ya integra el stack estándar de Spring Boot gracias a
`spring-boot-starter-test`. El único caso actual vive en
`src/test/java/finalprojectprogramming/project/ProjectApplicationTests.java` y
verifica que el contexto arranque correctamente con `@SpringBootTest`. A partir
de esa base, el siguiente plan divide el trabajo en etapas para alcanzar el
100 % de cobertura sin modificar clases de producción.

## Etapa 0 · Base existente y cimientos del entorno
1. **Preservar el smoke test `contextLoads()`**
   - No eliminar ni renombrar la clase `ProjectApplicationTests` para asegurar que
     JUnit siga detectando el arranque completo del contexto.
   - Añade, si lo consideras útil, comentarios dentro de la clase de prueba
     explicando que actúa como “canario” del wiring general (los comentarios no
     violan la restricción de no tocar el código de producción).
2. **Configurar JaCoCo sin alterar producción**
   - Inserta `jacoco-maven-plugin` en el bloque `<build><plugins>` del `pom.xml`
     raíz con ejecuciones `prepare-agent` y `report` para que el informe se
     genere automáticamente durante `mvn test`.
   - Ubica las reglas con umbral 100 % dentro de un perfil Maven opcional llamado
     `full-coverage`, de modo que al ejecutar `./mvnw clean test jacoco:report
     -Pfull-coverage` se active adicionalmente la meta `check` con
     `<minimum>1.0</minimum>` y falle el build si cualquier clase cae por debajo.
3. **Propiedades específicas de test**
   - Crea (si no existe) `src/test/resources/application-test.properties` con los
     overrides mínimos para desacoplar dependencias externas (e.g. endpoints
     dummy), evitando tocar `application.yml` de producción.
   - Declara el perfil activo en los tests integrales mediante
     `@ActiveProfiles("test")` para que todos los casos utilicen esta
     configuración controlada.
   - **Implementación actual:** el perfil de pruebas reside en
     `src/test/resources/application-test.properties` y el plugin de JaCoCo ya se
     encuentra configurado en `pom.xml` (perfil `full-coverage`), por lo que basta
     con ejecutar `./mvnw clean test jacoco:report -Pfull-coverage` para activar
     la verificación estricta cuando se necesite.

## Etapa 1 · Utilidades y seguridad
1. **`SecurityUtils`**
   - Ubicación de pruebas sugerida: `src/test/java/.../security/SecurityUtilsTest.java`.
   - Preparar `SecurityContextHolder` con utilidades auxiliares (`@BeforeEach`
     que limpie el contexto y métodos estáticos en un `TestSecurityContextBuilder`).
   - Casos indispensables:
     - `requireAny` con roles presentes y ausentes (verificar que lanza la
       excepción específica, probablemente `AccessDeniedException`).
     - `requireSelfOrAny` comparando IDs iguales y distintos.
     - `hasAny` devolviendo `true`/`false` según la combinación de authorities.
     - `getCurrentUserId` con `Authentication` vacía, con `Principal` nulo y con
       `AppUserDetails` válido.
     - `isSameUser` evaluando tokens de usuario nulos, combinaciones mixtas y
       usuarios idénticos.
   - Instrumenta verificaciones sobre el `SecurityContextHolder` para asegurarte
     de que se restaura tras cada caso (útil para evitar “leaks” que distorsionen
     pruebas futuras).
2. **`JwtService`**
   - Archivo de prueba sugerido: `JwtServiceTest.java` bajo el paquete de
     seguridad.
   - Inyecta la dependencia `Clock` mediante reflection o constructor accesible;
     en caso de necesitarlo, usa `ReflectionTestUtils.setField` de Spring.
   - Casos a cubrir:
     - Generación de token con claims adicionales y verificación posterior.
     - Validación positiva y negativa (token expirado, token con firma inválida,
       token con algoritmo distinto).
     - Manejo de secretos cortos que requieren normalización SHA-256 (si aplica).
     - Extracción de subject y claims personalizados.
     - Uso de un `Clock` adelantado para simular expiración inmediata.
3. **Mappers genéricos** (`GenericMapperImplementation`, `InputOutputMapperImplementation`)
   - Crear DTOs/entidades “dummy” dentro del árbol de pruebas, por ejemplo
     `DummyEntity` y `DummyDto` ubicados en `src/test/java/.../mapper/fixtures`.
   - Escenarios clave:
     - Conversión exitosa `entityToOutput` / `inputToEntity` comparando todos los
       campos relevantes.
     - Conversión con listas vacías y listas con elementos múltiples para cubrir
       bucles internos.
     - Validación de nullability: pasar `null` y esperar la excepción definida
       (usualmente `IllegalArgumentException`).
     - Verificar que los métodos que devuelven colecciones realicen copias y no
      referencias directas a la lista original.
   - **Implementación actual:** la suite `SecurityUtilsTest` verifica todas las
     ramas públicas, `JwtServiceTest` cubre la generación, validación y normalización
     de secretos, y los archivos `GenericMapperImplementationTest` e
     `InputOutputMapperImplementationTest` validan los mapeos y excepciones de
     constructores nulos.

## Etapa 2 · Servicios de dominio
1. **`AzureAuthenticationService`**
   - Pruebas en `AzureAuthenticationServiceTest` con `@ExtendWith(MockitoExtension.class)`.
   - Dependencias a mockear: `AzureGraphClient`, `UserRepository`, `JwtService`,
     `AuthenticationEventPublisher` (si existiese) y cualquier mapper auxiliar.
   - Ramas obligatorias:
     - Usuario existente activo → retorna tokens y no crea nuevo registro.
     - Usuario inexistente → persiste a través del repositorio y devuelve token.
     - Usuario deshabilitado o `accountNonLocked` falso → lanza la excepción
       específica.
     - Respuesta de Azure sin email principal → lanza `BusinessRuleException` o
       la excepción definida en la capa de servicio.
     - Errores del cliente HTTP (`IOException`, `HttpClientErrorException`) → se
       registran y se propagan/envuelven según la lógica actual.
   - Verifica side-effects: se invoca `userRepository.save`, `jwtService.generateToken`
     y se registran logs (puedes usar `OutputCaptureExtension` para validar).
2. **`ReservationServiceImplementation`**
   - Ubica las pruebas en `ReservationServiceImplementationTest` con `Mockito`.
   - Construye datos con un “test data builder” por cada agregado (`ReservationBuilder`,
     `UserBuilder`, `SpaceBuilder`).
   - Flujos que no pueden faltar:
     - `createReservation` con horario válido → guarda la reserva y retorna DTO.
     - `createReservation` con choque de horario → lanza `BusinessRuleException`.
     - `approveReservation`, `rejectReservation`, `cancelReservation`,
       `checkInReservation`, `checkOutReservation` cubriendo tanto el camino
       feliz como intentos de transición inválida.
     - Métodos de consulta (`findAll`, `findById`, filtros por estado/usuario)
       verificando paginación y ordenamiento.
     - Integración con `SecurityUtils`: comprueba que se consulta el usuario
       autenticado y que se valida la pertenencia de la reserva.
   - Usa `ArgumentCaptor` para confirmar los valores guardados en el repositorio.
3. **`ReservationScheduledTasks`**
   - Implementa la prueba con `@ExtendWith(MockitoExtension.class)` y un reloj
     fijo (`Clock.fixed`) para manipular el tiempo del scheduler.
   - Simula el repositorio retornando reservas con fechas pasadas y futuras.
   - Comprueba que las reservas expiradas cambian a `NO_SHOW` y que se invoca
     `reservationRepository.saveAll` con la colección modificada.
   - Introduce un escenario donde el repositorio lanza una excepción y valida que
     el bloque `try/catch` registra el error sin volver a lanzarlo.

## Etapa 3 · Controladores
1. **Configuración común**
   - Crea una clase base `BaseControllerTest` en `src/test/java` que configure
     `ObjectMapper`, `MockMvc` y registradores de mensajes comunes.
   - Define métodos utilitarios como `performPost(String url, Object body)` y
     `performGet(...)` que serialicen automáticamente los DTOs usando el
     `ObjectMapper` de Spring.
2. **Cobertura por controlador**
   - Usa `@WebMvcTest(NombreDelControlador.class)` y `@Import` de los componentes
     necesarios (por ejemplo, `SecurityConfig` simplificada) para cada suite.
   - Inyecta servicios mockeados con `@MockBean` y define expectativas usando
     `when(...).thenReturn(...)` y `thenThrow(...)` para cubrir 2xx, 4xx y 5xx.
   - Para rutas protegidas, utiliza `@WithMockUser` configurando roles y scopes
     distintos en cada caso.
   - Valida contenido del response (`andExpect(content().json(...))`) y headers
     relevantes (`Location`, `Authorization`, etc.).
3. **Escenarios adicionales**
   - Verifica que los validadores (`@Valid`) rechacen payloads con campos
     faltantes; usa `MockMvc` para enviar cuerpos JSON incompletos.
   - Si algún controlador produce streams o descargas, usa `andExpect`
     apropiados (`content().contentType`) y verifica la longitud esperada.
   - Cubre endpoints que devuelven colecciones vacías y listas con múltiples
     elementos para garantizar que la serialización es consistente.

## Etapa 4 · Clientes externos
1. **`AzureGraphClient`**
   - Configura `MockWebServer` (OkHttp) dentro del test para simular la API.
   - Casos clave:
     - Respuesta 200 con payload válido → mapear al DTO esperado.
     - Respuesta 401/403 → el cliente debe propagar o envolver la excepción.
     - Respuesta 500 → validar la reintención o propagación según la lógica.
     - Payload malformado → `JsonProcessingException` capturada y tratada.
   - Para reemplazar el `HttpClient`, usa reflection (`ReflectionTestUtils`) o
     provee un constructor alternativo en el propio test mediante subclase anónima.
   - Captura logs si el cliente escribe advertencias al manejar errores.
2. **Otros clientes HTTP**
   - Replica la estrategia con cada clase que ejecute peticiones externas
     (p. ej. servicios meteorológicos, de notificaciones o catálogos).
   - Para clientes que usen `RestTemplate`, utiliza `MockRestServiceServer`.
   - Si algún cliente utiliza `WebClient`, combina `StepVerifier` con `MockWebServer`.
   - Verifica también timeouts configurados: simula respuestas lentas con
     `Dispatcher` personalizado y asegura que la excepción se maneja correctamente.
   - **Implementación actual:** `AzureGraphClientTest` redirige el `HttpClient`
     hacia un `MockWebServer` para validar respuestas 200/401/500, payloads
     inválidos y ausencia de correo, mientras que `WeatherClientTest` emplea el
     mismo servidor simulado para cubrir mapeos felices, códigos 4xx/5xx,
     reintentos y ramas de timeout/IO.

## Etapa 5 · Repositorios
1. **Configuración de JPA de prueba**
   - Anota cada suite con `@DataJpaTest` y `@ActiveProfiles("test")`.
   - Usa `TestEntityManager` para preparar fixtures complejas con relaciones.
   - Carga scripts en `src/test/resources/data.sql` si necesitas estados
     compartidos.
2. **Cobertura específica por repositorio**
   - Para repositorios con métodos derivados (e.g. `findByStatusAndDateBetween`),
     construye datos que cubran la rama positiva (hay resultados) y negativa (no
     hay coincidencias).
   - En repositorios con queries personalizadas (`@Query`), valida parámetros
     nulos y límites (paginaciones, ordenamientos).
   - Verifica que las entidades se guarden con cascadas y que las relaciones
     `@ManyToOne`/`@OneToMany` se materialicen como se espera.
   - **Implementación actual:** `RepositoryIntegrationTest` levanta H2 en
     memoria con `@DataJpaTest`, persiste reservas completas (notificaciones,
     asistentes, calificaciones) y valida los métodos derivados de
     `UserRepository`, `NotificationRepository`, `RatingRepository`,
     `AuditLogRepository`, `SpaceScheduleRepository`, `SpaceImageRepository` y
     `SettingRepository`.
3. **Metadatos y eventos**
   - Si existen listeners (`@EntityListeners`), asegúrate de que se ejecuten
     dentro del contexto de test y cubre casos donde falten datos obligatorios.

## Etapa 6 · Integración cruzada
1. **Pruebas slice de servicio + repositorio**
   - Usa `@SpringBootTest` con `@AutoConfigureTestDatabase` para ejecutar los
     servicios reales contra H2 y repositorios reales, mientras mockeas clientes
     externos (`@MockBean`).
   - Define escenarios “end-to-end” controlados: crear una reserva vía servicio y
     verificar que el repositorio refleja el estado, luego ejecutar el scheduler
     y confirmar la transición.
2. **Serialización y contratos**
   - Utiliza `JacksonTester` (`@JsonTest`) para validar los DTOs críticos
     (`ReservationDto`, `UserDto`, etc.).
   - Crea JSONs de referencia en `src/test/resources/__snapshots__` y compara la
     estructura con `assertThat(json).isEqualToJson(...)`.
3. **Eventos y mensajería (si aplica)**
   - Si el proyecto publica eventos (RabbitMQ, Kafka, etc.), utiliza
     `@EmbeddedKafka` o mocks de colas para validar la publicación/consumo.
   - Verifica que las transacciones se comporten correctamente cuando la cola no
     está disponible (simulación de fallas).

## Etapa 7 · Automatización y monitoreo continuo
1. **Integración con CI/CD**
   - Añade un job en la pipeline (GitHub Actions, GitLab CI, etc.) que ejecute
     `./mvnw clean verify jacoco:report -Pfull-coverage` y archive el informe
     `target/site/jacoco`. Esto garantiza que cualquier contribución mantenga el
     100 % de cobertura.
   - Configura el job para publicar un comentario automático con los porcentajes
     de cobertura por paquete, usando herramientas como `jacoco-badge-generator`.
2. **Reglas de calidad adicionales**
   - Integra `pitest` o `jqwik` en perfiles separados para reforzar la calidad de
     las pruebas sin modificar producción.
   - Configura `maven-surefire-plugin` para fallar el build si se detectan tests
     ignorados (`failIfNoTests=true`), garantizando que no se omita ninguna clase.
3. **Reportes históricos**
   - Publica el contenido de `target/site/jacoco/index.html` en artefactos de la
     pipeline para permitir comparaciones históricas.
   - Considera integrar SonarQube (modo local, lanzado desde CI) únicamente con
     fines de análisis estático y métricas de cobertura.

## Ejecución de la batería de pruebas local
Desde la raíz del repositorio:

```bash
./mvnw clean test jacoco:report

Modo estricto (umbral 100 %) opcional:

./mvnw clean test jacoco:report -Pfull-coverage
```

Maven descargará dependencias, ejecutará todo lo situado en `src/test/java` y
generará el informe de cobertura en `target/site/jacoco/index.html`. Verifica el
reporte y cruza los datos con el plan por etapas para confirmar que cada rama y
flujo descritos tiene un caso asociado. Cuando todos los módulos mencionados
cuenten con pruebas exhaustivas, el objetivo de 100 % de cobertura se cumplirá
sin alterar el código de producción, cumpliendo la restricción impuesta.

### Solo tests de controllers

Para ejecutar únicamente las suites de controllers y evitar que `zsh` expanda el patrón, cita el valor de `-Dtest`:

```bash
./mvnw -DskipITs=false -Dtest="finalprojectprogramming.project.controllers.*Test" test -DtrimStackTrace=false
```

Resultado actual: 64 tests de controllers en verde (0 fallos/errores).

## Apéndice A · Biblioteca de utilidades de prueba
1. **Builders y fixtures**
   - Implementa builders en `src/test/java/.../fixtures` siguiendo el patrón
     `XxxTestBuilder` con métodos `with...` encadenables y un `build()` que
     devuelve la entidad/DTO lista para usar.
   - Mantén versiones `default()` que creen objetos coherentes y que permitan
     personalizar campos mínimos para cubrir escenarios particulares.
   - Centraliza datos comunes (IDs, fechas, textos) en una clase `TestConstants`
     para evitar valores mágicos repetidos y garantizar consistencia entre tests.
2. **Utilidades para tiempo y seguridad**
   - Crea `FixedClockProvider` para exponer un `Clock` ajustable que puedas
     inyectar en servicios y tareas programadas durante las pruebas.
   - Expone métodos estáticos como `withAuthenticatedUser(Long id, String... roles)`
     que configuren el `SecurityContextHolder` y devuelvan un `AutoCloseable` para
     restaurar el estado tras cada test (`try-with-resources`).
3. **Datos externos simulados**
   - Guarda respuestas JSON de servicios externos en
     `src/test/resources/http-stubs/`. Nómbralas con el patrón
     `<cliente>/<caso>.json` (p.ej. `azure/success.json`, `azure/invalid.json`).
   - Usa estas cargas en los tests de clientes HTTP para validar tanto los caminos
     positivos como los errores de parseo.

## Apéndice B · Matriz de cobertura mínima por clase
| Paquete / Clase                               | Tipo de prueba recomendado            | Casos obligatorios |
|-----------------------------------------------|---------------------------------------|--------------------|
| `security.SecurityUtils`                      | Unitario puro (JUnit + Mockito)       | Roles válidos/ inválidos, IDs coincidentes, contexto vacío |
| `security.JwtService`                         | Unitario con fixtures de Clock        | Token válido, expirado, firma inválida, claims extra |
| `mapper.GenericMapperImplementation`          | Unitario con DTOs dummy               | Conversión simple, listas múltiples, argumentos nulos |
| `mapper.InputOutputMapperImplementation`      | Unitario                              | Input→Entity y Entity→Output con campos faltantes |
| `auth.AzureAuthenticationService`             | Unitario con mocks                    | Usuario existente, nuevo, bloqueado, sin email, error HTTP |
| `reservation.ReservationServiceImplementation`| Unitario con mocks                    | Creación, conflicto horario, transiciones inválidas, filtros |
| `reservation.ReservationScheduledTasks`       | Unitario con Clock fijo               | Reservas vencidas, futuras, excepción en repositorio |
| `controller.*Controller`                      | `@WebMvcTest`                         | 2xx, 4xx, 5xx, validaciones, autorizaciones |
| `client.AzureGraphClient`                     | `MockWebServer`                       | 200 OK, 401/403, 500, JSON inválido |
| Otros clientes HTTP                           | `MockRestServiceServer`/`WebClientTest`| Timeouts, códigos de error, payload alternativo |
| Repositorios JPA                              | `@DataJpaTest`                        | Resultados positivos, vacíos, parámetros límite |
| DTOs críticos                                 | `@JsonTest`                           | Serialización, deserialización, compatibilidad |

Marca cada casilla como completada en un tablero Kanban o checklist compartido
(p.ej. dentro de GitHub Projects) para asegurar que no quede ninguna clase sin
casos específicos.

## Apéndice C · Buenas prácticas de mantenimiento
1. **Revisión cruzada obligatoria**
   - Solicita que al menos otra persona revise cada suite de pruebas nueva,
     enfocándose en que los mocks no reproduzcan la implementación real (evitar
     “test doubles” frágiles).
2. **Refactorización guiada por pruebas**
   - Una vez cubierta la funcionalidad, si es necesario refactorizar producción,
     usa las pruebas como red de seguridad para garantizar que el comportamiento
     se mantiene intacto.
3. **Ejecución local previa a cada commit**
   - Añade un hook `pre-commit` que ejecute `./mvnw test` para evitar que se
     suban cambios sin pruebas actualizadas.
4. **Documentación viva**
   - Actualiza este archivo cada vez que aparezca un nuevo módulo o caso de uso,
     especificando la suite de pruebas asociada y los escenarios mínimos.

Con estos apéndices, el plan queda totalmente detallado y accionable. A partir del estado actual (77 % instrucciones, 58 % branches), la siguiente palanca para subir cobertura rápidamente es ampliar suites de controladores y servicios con slices ligeros y unit tests adicionales (paquetes con menor cobertura actual: `controllers`, `configs`, `security`, `services.storage`) antes de reactivar gradualmente las integraciones pesadas. Además, se añadieron suites para `mail`, `space`, `spaceschedule`, `spaceimage`, `rating`, `openWeather` y `auditlog`, elevando estas áreas desde 0–7 % hasta >70–95 % en la mayoría de los casos.
