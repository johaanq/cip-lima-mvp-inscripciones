# MVP Inscripciones — Evento Día del Padre (CIP Lima)

Monorepo con backend REST, frontend Vue 3 y orquestación Docker para gestionar inscripciones al evento institucional del Consejo Departamental de Lima. El foco del reto es la **correctitud de las reglas de negocio**, la **estabilidad del flujo de datos**, la **concurrencia de cupos** y las **decisiones arquitectónicas** documentadas — no la estética de la interfaz.

Repositorio: [github.com/johaanq/cip-lima-mvp-inscripciones](https://github.com/johaanq/cip-lima-mvp-inscripciones)

---

## Requisitos previos

- Docker Desktop 4.x o superior
- Docker Compose v2
- (Opcional) Java 17 y Node.js 22+ para desarrollo local sin contenedores

---

## Arranque en un comando

```bash
cp .env.example .env
docker compose up --build
```

Para reiniciar desde cero (incluye volúmenes de datos):

```bash
docker compose down -v
docker compose up --build
```

El backend espera a que PostgreSQL, MinIO y el mock de colegiados estén saludables antes de arrancar. No se requieren pasos manuales adicionales.

---

## URLs y servicios

| Servicio | URL | Notas |
|----------|-----|-------|
| Portal de inscripción | http://localhost | Frontend (nginx) |
| Panel administrador | http://localhost/admin/login | Requiere JWT |
| API REST | http://localhost:8080/api | Backend Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | Documentación OpenAPI |
| MinIO consola | http://localhost:9001 | Usuario/clave en `.env.example` |
| Mock colegiados | Red interna Docker (`colegiados-mock:3001`) | No expuesto al host |

El frontend en nginx proxya `/api` hacia el backend, por lo que las peticiones del navegador pueden usar rutas relativas `/api/...`.

---

## Credenciales por defecto

### PostgreSQL

| Variable | Valor |
|----------|-------|
| Base de datos | `inscripciones` |
| Usuario | `postgres` |
| Contraseña | `postgres` |
| Host (desde host) | `localhost:5432` no expuesto; solo red Docker |

### Administrador de la aplicación

| Campo | Valor |
|-------|-------|
| Usuario | `admin` |
| Contraseña | `admin123` |

### MinIO

| Campo | Valor |
|-------|-------|
| Access key | `minioadmin` |
| Secret key | `minioadmin` |
| Bucket | `inscripciones-imagenes` |

Todas las variables están documentadas en `.env.example`. **No commitear** el archivo `.env` con secretos reales.

---

## Arquitectura del sistema

### Vista de contenedores

```text
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Frontend Vue   │────▶│  Spring Boot API │────▶│   PostgreSQL    │
│  (nginx :80)    │     │  (:8080)         │     │   (interno)     │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
           ┌──────────────┐ ┌──────────┐ ┌──────────────┐
           │ json-server  │ │  MinIO   │ │ Swagger UI   │
           │ colegiados   │ │ (S3 API) │ │ (en backend) │
           └──────────────┘ └──────────┘ └──────────────┘
```

### Arquitectura de software (backend)

Se adoptó **arquitectura en capas** en lugar de DDD completo: el dominio es acotado (inscripción, cupo, elegibilidad) y el tiempo del reto prioriza claridad y transacciones correctas sobre ceremonia de agregados o eventos de dominio.

```text
┌─────────────────────────────────────────────────────────┐
│  Presentación — Controllers REST + DTOs                 │
├─────────────────────────────────────────────────────────┤
│  Aplicación — Services (@Transactional)                 │
├─────────────────────────────────────────────────────────┤
│  Dominio ligero — Entidades JPA, enums, reglas puras  │
├─────────────────────────────────────────────────────────┤
│  Infraestructura — Repositorios, HTTP client, MinIO     │
└─────────────────────────────────────────────────────────┘
```

### Estructura del monorepo

```text
cip-lima-mvp-inscripciones/
├── backend/              # Java 17, Spring Boot 3.5.16
├── frontend/             # Vue 3 + Vite
├── mock/db.json          # Datos del mock de colegiados
├── docker-compose.yml
├── .env.example
└── README.md
```

### Patrones aplicados

| Patrón | Implementación | Propósito |
|--------|----------------|-----------|
| Layered Architecture | Paquetes `controller`, `service`, `domain`, `repository` | Separación de responsabilidades |
| Repository | `SolicitudRepository`, `EventoConfigRepository` | Abstraer persistencia |
| Service Layer | `InscripcionService`, `AdminSolicitudService` | Casos de uso y transacciones |
| DTO | Records en `dto/` | No exponer entidades JPA en la API |
| Gateway / Client | `ColegiadosApiClient` | Aislar la API externa simulada |
| Strategy (ligero) | `ColegiadoValidationService` + reglas independientes | Reglas de elegibilidad extensibles |
| Adapter | AWS SDK S3 apuntando a MinIO | Storage compatible con S3 on-premise |

### Flujo de datos principal

**Inscripción pública**

1. El frontend consulta `GET /api/evento/estado` para verificar aforo.
2. El usuario envía `POST /api/inscripciones` (multipart: DNIs, nombre, imagen).
3. El backend valida aforo, sube la imagen a MinIO y consulta el mock de colegiados.
4. `ColegiadoValidationService` evalúa las reglas; resultado `PENDIENTE` o `RECHAZADO` (automático) persistido en PostgreSQL.

**Aprobación administrativa**

1. Admin autenticado con JWT consulta métricas y pendientes.
2. Al aprobar, en **una sola transacción**: incremento condicional de cupo + cambio de estado a `APROBADO` + log de invitación simulado.
3. Al rechazar, observación obligatoria, estado `RECHAZADO` con `origen_rechazo=ADMIN` + log de alerta simulado.

---

## Decisiones técnicas (stack)

| Capa | Elección | Motivo |
|------|----------|--------|
| Backend | Java 17 + Spring Boot 3.5.16 | Transacciones declarativas, ecosistema maduro, capas claras |
| Base de datos | PostgreSQL 16 | ACID, constraints, adecuada para concurrencia de cupos |
| Migraciones | Flyway (`V1__init_schema.sql`) | Persistencia formal en el ciclo de vida (requerimiento del reto) |
| Mock colegiados | json-server en Docker | Sugerido en la especificación; cero código custom |
| Imágenes | MinIO (API S3) | Object storage on-premise; migración a AWS S3 cambiando endpoint |
| Seguridad admin | Spring Security + JWT stateless | Protección de `/api/admin/**` sin sesión en servidor |
| Documentación API | SpringDoc OpenAPI 3 | Contrato visible para evaluadores; esquema Bearer JWT |
| Frontend | Vue 3 + Vite | SPA mínima: formulario + dashboard |
| Orquestación | Docker Compose | Un comando levanta todo el stack |

---

## Registros de decisiones arquitectónicas (ADR)

### ADR-001: Arquitectura en capas en lugar de DDD completo

**Contexto:** Reto de 8 horas con dominio pequeño (inscripción, cupo, validación de colegiado).

**Decisión:** Capas (controller → service → domain/repository → infra) con reglas de negocio en servicios y métodos de entidad, sin agregados ni bounded contexts.

**Consecuencias:** Menor overhead; README y código legibles para el evaluador. Si el dominio crece, se pueden extraer agregados más adelante.

### ADR-002: Persistir auto-rechazos como `RECHAZADO`

**Contexto:** Una inscripción no elegible puede descartarse o persistirse.

**Decisión:** Toda solicitud enviada se persiste. Los rechazos automáticos quedan en `RECHAZADO` con `origen_rechazo=AUTOMATICO` y motivo explícito.

**Consecuencias:** Métricas del dashboard coherentes, trazabilidad auditable y flujo de datos estable (siempre hay registro en BD).

### ADR-003: Concurrencia de cupos con UPDATE condicional

**Contexto:** Varios administradores podrían aprobar simultáneamente con cupo limitado (10 plazas).

**Decisión:** En la misma transacción de aprobación, ejecutar:

```sql
UPDATE evento_config
SET cupo_ocupado = cupo_ocupado + 1
WHERE id = 1 AND cupo_ocupado < cupo_maximo;
```

Si `rows affected = 0`, no se aprueba la solicitud (HTTP 409). El cupo solo incrementa al aprobar, nunca al inscribirse.

**Consecuencias:** Evita sobreventa sin bloqueo pesimista explícito; suficiente para el volumen del MVP.

### ADR-004: MinIO en lugar de filesystem o AWS S3 directo

**Contexto:** Imágenes del DNI del menor deben almacenarse fuera de la BD.

**Decisión:** MinIO en Docker con AWS SDK (path-style). Clave del objeto en columna `imagen_object_key`.

**Consecuencias:** Mismo patrón que S3 en producción; el bucket se crea al arranque del backend si no existe.

### ADR-005: JWT en memoria (frontend) y Spring Security (backend)

**Contexto:** Panel admin requiere autenticación stateless.

**Decisión:** Login vía `POST /api/auth/login`; token JWT solo en memoria del SPA (no `localStorage`). Filtro JWT en Spring Security para rutas `/api/admin/**`.

**Consecuencias:** Reduce riesgo de XSS persistente; el token se pierde al cerrar pestaña (aceptable para MVP admin).

### ADR-006: SpringDoc OpenAPI con Bearer auth

**Contexto:** Evaluadores necesitan probar endpoints admin sin leer todo el código.

**Decisión:** SpringDoc en `/swagger-ui/index.html` con esquema `bearerAuth` en operaciones administrativas.

**Consecuencias:** Documentación viva alineada al código; facilita la revisión técnica.

---

## Persistencia

### Migraciones Flyway

- Ubicación: `backend/src/main/resources/db/migration/`
- Estrategia JPA: `ddl-auto: validate` (el esquema lo define Flyway, no Hibernate)

### Tablas

**`evento_config`** (singleton, `id = 1`)

| Columna | Descripción |
|---------|-------------|
| `cupo_maximo` | Aforo estricto (10 en el MVP) |
| `cupo_ocupado` | Incrementa solo al aprobar |
| `sede_consejo` | Consejo territorial del evento (`Lima`) |

**`solicitud_inscripcion`**

| Columna | Descripción |
|---------|-------------|
| `dni_colegiado`, `nombre_colegiado`, `dni_menor` | Datos del formulario |
| `imagen_object_key` | Referencia al objeto en MinIO |
| `estado` | `PENDIENTE`, `APROBADO`, `RECHAZADO` |
| `motivo_rechazo` | Obligatorio si `RECHAZADO` (CHECK en BD) |
| `origen_rechazo` | `AUTOMATICO` o `ADMIN` |
| `created_at`, `updated_at` | Trazabilidad |

Índices: `estado`, `dni_colegiado`, `created_at DESC`.

### Máquina de estados

```text
                    ┌──────────────┐
   inscripción ────▶│  PENDIENTE   │──── admin aprueba ────▶ APROBADO
   (elegible)        └──────┬───────┘
                            │
                            │ admin rechaza (observación obligatoria)
                            ▼
                        RECHAZADO ◀──── auto-rechazo (reglas API colegiados)
```

---

## Concurrencia y aforo

### Bloqueo en nuevas inscripciones

Antes de registrar, `InscripcionService` consulta `evento_config`. Si `cupo_ocupado >= cupo_maximo`, lanza `AforoCompletoException` (HTTP 409). Las solicitudes en estado `PENDIENTE` no consumen cupo hasta ser aprobadas.

### Aprobación concurrente

Pseudocódigo del caso de uso:

```text
@Transactional
aprobar(solicitudId):
  solicitud = buscar(solicitudId)
  filas = UPDATE evento_config SET cupo_ocupado = cupo_ocupado + 1
          WHERE id = 1 AND cupo_ocupado < cupo_maximo
  si filas == 0 → error "No hay cupo disponible"
  solicitud.aprobar()
  guardar(solicitud)
  notificarInvitacion(solicitud)  // log simulado
```

Implementación JPA equivalente en `EventoConfigRepository.incrementarCupoSiDisponible`.

---

## Reglas de negocio

Evaluadas contra la API mock de colegiados al momento de inscribirse:

| Criterio | Condición | Resultado |
|----------|-----------|-----------|
| Colegiado registrado | DNI existe en API mock | Si no existe → `RECHAZADO` automático |
| Estado habilitado | `habilitado: true` | Si false → `RECHAZADO` automático |
| Pertenencia territorial | `consejo_departamental` = sede del evento (`Lima`) | Si distinto → `RECHAZADO` automático |
| Restricción laboral | `es_administrativo: false` | Si true → `RECHAZADO` automático |
| Aforo disponible | `cupo_ocupado < cupo_maximo` | Si lleno → error HTTP 409 (no persiste) |
| Elegible | Pasa todas las reglas | `PENDIENTE` (revisión admin) |

**Acciones del administrador (solo sobre `PENDIENTE`):**

- **Aprobar:** consume cupo, estado `APROBADO`, log de invitación.
- **Rechazar:** observación obligatoria, estado `RECHAZADO`, `origen_rechazo=ADMIN`, log de alerta.

---

## API mock de colegiados

Servicio `colegiados-mock` (json-server) expone `GET /colegiados?dni={dni}` desde `mock/db.json`.

El backend (`ColegiadosApiClient`) consulta el listado filtrado y selecciona el registro coincidente. Esto evita depender de rutas custom de json-server.

URL interna Docker: `http://colegiados-mock:3001` (variable `COLEGIADOS_API_URL`).

---

## Datos de prueba

| DNI | Nombre | Resultado esperado al inscribirse | Motivo |
|-----|--------|-----------------------------------|--------|
| `12345678` | Juan Perez | `PENDIENTE` | Elegible (Lima, habilitado, no admin) |
| `87654321` | Maria Lopez | `RECHAZADO` | Personal administrativo |
| `11223344` | Carlos Ruiz | `RECHAZADO` | No habilitado |
| `44332211` | Ana Gomez | `RECHAZADO` | Consejo distinto de Lima |
| `99887766` | Pedro Infante | `PENDIENTE` | Elegible (caso adicional) |

Para probar aforo completo: aprobar 10 solicitudes pendientes; la inscripción 11 debe recibir error de aforo.

---

## Seguridad

| Aspecto | Implementación |
|---------|----------------|
| Rutas públicas | `/api/inscripciones`, `/api/evento/**`, `/api/auth/login`, `/api/health`, Swagger |
| Rutas protegidas | `/api/admin/**` (rol `ADMIN`) |
| JWT | Firmado con `JWT_SECRET`; expiración configurable (`JWT_EXPIRATION_MS`) |
| Contraseña admin | Validada con BCrypt vía Spring Security |
| CORS | Origen explícito (`CORS_ALLOWED_ORIGINS`) |
| Imágenes | Upload validado (tipo/tamaño); lectura de imagen solo con JWT admin |
| Secretos | Variables en `.env`; `.env.example` sin valores de producción |

---

## Swagger / OpenAPI

1. Abrir http://localhost:8080/swagger-ui/index.html
2. Ejecutar `POST /api/auth/login` con `{ "username": "admin", "password": "admin123" }`
3. Copiar el `token` de la respuesta
4. Pulsar **Authorize** e ingresar: `Bearer <token>`
5. Probar endpoints bajo tag **Administracion**

---

## API REST (resumen)

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/health` | No | Health check |
| GET | `/api/evento/estado` | No | Cupo y aforo |
| POST | `/api/inscripciones` | No | Registro multipart |
| GET | `/api/inscripciones/{id}` | No | Consulta de solicitud |
| POST | `/api/auth/login` | No | Login admin → JWT |
| GET | `/api/admin/metricas` | JWT | Contadores del dashboard |
| GET | `/api/admin/solicitudes/pendientes` | JWT | Listado pendientes |
| GET | `/api/admin/solicitudes/{id}/imagen` | JWT | Stream imagen MinIO |
| POST | `/api/admin/solicitudes/{id}/aprobar` | JWT | Aprobar con cupo |
| POST | `/api/admin/solicitudes/{id}/rechazar` | JWT | Rechazar con observación |

---

## Desarrollo local (sin Docker)

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Requiere PostgreSQL, MinIO y json-server en ejecución con las URLs de `application.yml` / `.env`.

Tests del backend:

```bash
cd backend
./mvnw test
```

---

## Historial Git

El reto exige commits **atómicos y descriptivos** (mismo peso que el código). Ejemplos del historial:

```text
feat(backend): proyecto Spring Boot inicial...
feat(db): migración Flyway inicial de tablas
feat(domain): reglas de elegibilidad del colegiado
feat(api): endpoint de inscripcion con auto-rechazo
feat(security): autenticacion JWT con Spring Security
feat(storage): integracion MinIO...
feat(admin): metricas, listado y acciones sobre solicitudes
chore(docker): stack completo con docker compose up
feat(frontend): portal de inscripcion
feat(frontend): login, dashboard y diseño institucional
docs: README con arquitectura, persistencia y concurrencia
```

Cada commit representa una pieza lógica terminada, no un dump masivo al final.

---

## Limitaciones y mejoras futuras

- Refresh token y rotación de JWT para sesiones admin más largas
- Rate limiting en login y en inscripciones públicas
- Cola de correo real (SQS + SES o similar) en lugar de logs simulados
- Validación estricta nombre formulario vs. nombre en API colegiados
- Tests de integración con Testcontainers (PostgreSQL + MinIO)
- CI con GitHub Actions (build + tests en cada push)

---

## Atributos de calidad priorizados

| Atributo | Prioridad | Cómo se aborda |
|----------|-----------|----------------|
| Correctitud / reglas de negocio | Crítica | Reglas aisladas, auto-rechazos persistidos, constraints SQL |
| Consistencia de datos | Crítica | Transacciones `@Transactional`, update condicional de cupo |
| Trazabilidad | Alta | Flyway, `origen_rechazo`, timestamps, historial Git |
| Mantenibilidad | Alta | Capas, ADRs, OpenAPI, README |
| Seguridad | Alta | JWT, BCrypt, CORS, uploads validados |
| Disponibilidad | Media | Healthchecks en Docker Compose |
| Escalabilidad | Documentada | API stateless; BD y storage desacoplados |
