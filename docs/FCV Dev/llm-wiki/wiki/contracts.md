# Contratos REST

## HECHO

El PRD exige REST/JSON entre `citas-web` y `citas-api` y validación cross-repo en funcionalidades clave.

## DECISIÓN — 2026-09-17 · HU-004/005/006/007

El contrato inicial cubre solamente autenticación bajo `/api/v1/auth`:

| Operación | Entrada | Éxito |
|---|---|---|
| `POST /register` | JSON `firstName`, `lastName`, `documentType`, `documentNumber`, `email`, `phone`, `password` | `201`, JSON con `id`, datos públicos y rol `USER`, sin contraseña |
| `POST /login` | JSON `email`, `password` | `200`, JSON `accessToken`, `tokenType=Bearer`, `expiresIn`; cookie `refresh_token` |
| `POST /refresh` | Cookie `refresh_token` | `200`, nuevo access en JSON y nueva cookie refresh; la anterior se revoca |
| `POST /logout` | Cookie `refresh_token` | `204`, revocación de la sesión y cookie borrada |

Email se normaliza con trim y minúsculas. Documento es único por `(documentType, documentNumber)` normalizados. La contraseña de registro es obligatoria y se limita a 72 bytes UTF-8 por el límite de BCrypt; sus espacios no se alteran. Solo se permite autoregistro `USER`. Errores: `400` validación, `409` duplicidad, `401` credencial/refresh inválido, `403` rol insuficiente, en formato Problem Details; login no revela qué credencial falló.

Access JWT y refresh JWT usan secretos distintos, tipo explícito y duraciones configurables (valores iniciales: 15 minutos y 7 días). El access lleva `sub` y roles. El refresh lleva `sub` y `jti`; su identificador se guarda solo como hash en una sesión persistida. Un refresh válido rota ambos tokens atómicamente. Logout revoca solo la sesión indicada; un access emitido conserva validez hasta su expiración.

Para sitios distintos, la cookie es `HttpOnly; Secure; SameSite=None`, con `Path=/api/v1/auth`. CORS permite credenciales únicamente al `FRONTEND_ORIGIN` configurado. Login, refresh y logout requieren `Origin` permitido cuando se envía y `X-Requested-With: XMLHttpRequest`; el perfil HTTP local usa cookie `SameSite=Lax` sin `Secure`. El cliente futuro deberá enviar credenciales y ese encabezado, guardar access únicamente según su diseño aprobado y eliminar su estado local al salir.

### Impacto cross-repo antes del cambio REST

- `citas-api`: nuevo `pom.xml`, código de dominio/aplicación/adaptadores, migración Flyway, configuración, pruebas y este contrato.
- `citas-web`: sin cambios en este incremento; HU-033 integrará las cuatro rutas, cookie y errores. Al ser endpoints nuevos, no hay cliente previo que migrar.
- Compatibilidad: `/api/v1` fija la versión de este contrato; cambios posteriores requieren revisión de ambas partes. Migración: esquema inicial de identidad por Flyway. Pruebas: REST, seguridad, persistencia y `mvn test` en backend; prueba cross-repo cuando exista el cliente.

## PREGUNTA ABIERTA

Las rutas, filtros, paginación y formatos de fecha/hora de las demás HU siguen sin contrato aprobado.

## DECISIÓN — 2026-09-22 · Contrato S3 de agendamiento

Todos los recursos S3 usan `/api/v1`, JWT access en `Authorization: Bearer` y JSON. Las fechas se representan como `YYYY-MM-DD` y las horas como `HH:mm` en la zona `America/Bogota`.

| Recurso | Operación | Rol |
|---|---|---|
| Catálogos | `GET /catalogs/{locations|appointment-statuses|roles|regimes|plans}` | autenticado |
| Especialidades disponibles | `GET /specialties` | autenticado |
| Especialidades ADMIN | `GET|POST|PATCH /admin/specialties[/{id}]` | ADMIN |
| Profesionales | `POST /admin/professionals`; `PUT /admin/professionals/{id}/specialties|locations`; `PATCH /admin/professionals/{id}/active` | ADMIN |
| Bloques propios | `GET|POST /professional/availability-blocks`; `PATCH|DELETE /professional/availability-blocks/{id}` | PROFESSIONAL |
| Disponibilidad | `GET /availability?locationId=&specialtyId=&date=&professionalId?` | USER |
| Reserva | `POST /appointments` | USER |
| Solicitudes especializadas | `GET /admin/appointments/pending-specialized`; `POST /admin/appointments/{id}/decision` | ADMIN |

`POST /auth/register` acepta `insurancePlanId` opcional; la afiliación es administrativa y no modifica las reglas de agenda. `POST /appointments` recibe `professionalId`, `locationId`, `specialtyId`, `date`, `startTime` y `reason` opcional. La API deriva la naturaleza general o especializada desde la especialidad: devuelve `APPROVED` para general y `REQUESTED` para especializada. Una decisión ADMIN recibe `APPROVE` o `REJECT`; el rechazo exige `reason`.

Errores de validación usan `400`; recursos o relaciones inexistentes usan `404`; rol u ownership usan `403`; slots ocupados, selección inválida o transición no permitida usan `409`. El frontend consume estas rutas directamente, sin BFF, y no guarda citas ni slots como fuente de verdad.

## DECISIÓN — 2026-09-22 · Corte web de autenticación

`citas-web` consume las cuatro operaciones de autenticación directamente con `VITE_API_URL` (valor local: `http://localhost:8080`). Envía `credentials: include` y `X-Requested-With: XMLHttpRequest` en login, refresh y logout. El access JWT permanece solo en memoria; el refresh se mantiene en cookie `HttpOnly` y se rota al restaurar la sesión. La interfaz no registra ni muestra tokens o contraseñas.

El CORS permite exclusivamente `FRONTEND_ORIGIN`, métodos `POST`, `GET`, `OPTIONS`, encabezados `Content-Type`, `Authorization`, `X-Requested-With` y credenciales. La base de referencia ya existente utiliza `BIGINT` para usuarios, `roles.code` y `refresh_tokens`; Flyway hace baseline en versión 0 y `V1` es compatible con ese esquema 3FN.

## DECISIÓN — 2026-09-23 · Contrato S4 de ciclo de vida

Todas las rutas usan `/api/v1`, JSON, JWT access y fecha/hora local de `America/Bogota`.

| Recurso | Operación | Rol |
|---|---|---|
| Recuperación | `POST /auth/password-recovery`, `POST /auth/password-reset` | pública |
| Perfil | `GET|PATCH /users/me` | autenticado |
| EPS y planes | `GET|POST|PATCH /admin/eps`, `GET|POST|PATCH /admin/eps-plans` | ADMIN |
| Citas propias | `GET /appointments`, `GET /appointments/{id}`, `POST /appointments/{id}/cancel`, `POST /appointments/{id}/reschedule-requests`, `GET /appointments/{id}/history` | USER |
| Agenda y cierre | `GET /professional/appointments`, `POST /professional/appointments/{id}/closure` | PROFESSIONAL |
| Bandeja y decisiones | `GET /admin/inbox`, `POST /admin/reschedule-requests/{id}/decision` | ADMIN |
| Próximas citas | `GET /admin/appointments/upcoming` | ADMIN |

La recuperación responde `202` sin revelar la existencia de la cuenta. En perfil `local`, un ADMIN puede consumir una sola vez el token de prueba con `GET /auth/local/password-reset-mailbox?email=`; ese token vive solo en memoria y nunca se registra. `PATCH /users/me` permite únicamente `phone`.

Las reprogramaciones retienen slots mediante `professional_slots.reschedule_request_id`. Una aprobación libera los slots originales y asigna los retenidos; un rechazo libera solo la retención. La bandeja admite filtros opcionales `locationId`, `professionalId`, `specialtyId` y `date`. La consulta de próximas citas usa `from`, `to` y `locationId` opcional; es la fuente de lectura para n8n y no cambia el núcleo.

## S5 — Documentación OpenAPI y webhook n8n

- Swagger UI: `/swagger-ui/index.html`; OpenAPI JSON: `/v3/api-docs`. Solo estas rutas son públicas; las operaciones exigen Bearer JWT (`bearerAuth`).
- Webhook saliente opcional, desactivado por defecto: `N8N_WEBHOOK_ENABLED`, `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_BEARER_TOKEN`. Con `enabled=true` sin URL o token la API falla al iniciar.
- Solicitud `POST` con `Authorization: Bearer <token>` y payload mínimo sin PII: `schemaVersion`, `eventId`, `eventType`, `appointmentId`, `status`, `source`, `occurredAt`.
- Eventos emitidos: `APPROVED`/`REJECTED` decididos por ADMIN y `CANCELLED` por USER. No se emiten la aprobación automática de medicina general (`SYSTEM`) ni los cierres clínicos.
- Se envía después del commit; un fallo de red o un no-2xx solo genera un log técnico y no revierte la cita. Sin reintentos persistentes.
