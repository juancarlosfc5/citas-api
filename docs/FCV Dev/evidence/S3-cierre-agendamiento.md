# Evidencia — cierre del núcleo de agendamiento (S3)

**Fecha:** 2026-09-25 · **Plan:** `PLAN_CIERRE_AGENDAMIENTO_S3.md` (raíz del workspace)
**Ramas:** `citas-api@develop` (base `34392dd`), `citas-web@develop` (base `5438094`). Datos 100 % sintéticos (`*@example.test`); sin credenciales ni tokens en esta evidencia.

## Defectos corregidos

| # | Hallazgo | Corrección | Prueba |
|---|---|---|---|
| 1 | `reserve` no consideraba `reschedule_request_id` al validar ocupación. | La comprobación incluye `appointment_id` **o** `reschedule_request_id`. | `SchedulingClosureIntegrationTest.slotRetainedByRescheduleRequestCannotBeBooked` |
| 1b | **Doble reserva concurrente** (hallado por la nueva prueba): la verificación de ocupación era una lectura no bloqueante sobre la instantánea InnoDB (REPEATABLE READ) y dos transacciones confirmaban la misma franja. | La verificación pasa a lectura bloqueante (`... for update`), que lee la última versión confirmada. | `concurrentBookingsOfSameSlotProduceExactlyOneAppointment` (fallaba 2≠1 antes del cambio) |
| 1c | Decidir una cita ya decidida devolvía `500`. | `decide` lanza conflicto → `409` (transición inválida). | `adminApprovalKeepsSlotsAndRejectionReleasesThem`; REST en vivo `409` |
| 2 | Modal USER: fecha solo tras elegir profesional, iniciando en hoy. | Orden sede/especialidad/fecha → profesional → franja → confirmación; reconsulta y limpia selección al cambiar filtros; muestra `APPROVED`/`REQUESTED`; ante `409` vuelve a franjas y refresca. | `BookAppointmentModal.test.tsx` (4) |
| 2b | IDs de catálogo numéricos comparados con valores `<select>` (texto): la confirmación quedaba inerte. | `catalogsApi` normaliza `id` a texto. | `schedulingClosure.test.ts` |
| 3 | ADMIN usaba `/admin/inbox` (sin `id`, mezcla reprogramaciones). | Usa `/admin/appointments/pending-specialized` y decide con `id`; motivo de rechazo obligatorio en formulario visible. `/admin/inbox` se conserva en el cliente. | `schedulingClosure.test.ts` |
| 4 | Mapeo de bloques. | JSON real confirmado: `{id, locationId, date, start:"HH:mm:ss", end}`; coincide con `toBlock`. Sin cambio de contrato. La UI muestra nombre de sede. | REST en vivo + `schedulingClosure.test.ts` |
| 5 | Horas con `new Date(startAt)` (dependía de la zona del navegador). | `bogotaTime.ts` trata `YYYY-MM-DDTHH:mm` como texto local America/Bogota. | `schedulingClosure.test.ts` |

Sin migraciones nuevas ni cambios contractuales.

## Pruebas automatizadas

- Backend: `docker compose -f compose.test.yml run --rm api-test mvn test` → **22/22 PASS** (incluye 6 escenarios nuevos con MySQL 8.4 Testcontainers: 30/60 min y hueco entre bloques, general `APPROVED`/`SYSTEM`, especializada `REQUESTED`/`USER` con 2 slots, aprobación conserva slots y rechazo los libera con `ADMIN`, profesional/sede/especialidad no habilitados y franja que cruza el fin de bloque, slot retenido por reprogramación, concurrencia).
- Nota de entorno: `docker compose exec citas-api-dev mvn test` falla con "Could not find a valid Docker environment" (el contenedor dev no monta el socket); se usa `compose.test.yml`.
- Frontend (host Windows): `npm run lint` PASS · `npm test` **21/21 PASS** · `npm run build` PASS. En el contenedor `citas-web-dev` los binarios de `node_modules` son de Windows (`node.exe: not found`); se ejecutó en el host.

## Recorrido REST + MySQL (API local, perfil `local`)

Cuentas sintéticas creadas por `/auth/register` y `/admin/professionals`. **Bootstrap:** no existe endpoint para crear el primer ADMIN; se asignó el rol a un usuario sintético con un `INSERT` en `user_roles`. Profesional id 9, especialidad 60 min id 14, sede id 2, fecha 2026-09-29.

| Paso | Resultado |
|---|---|
| PROFESSIONAL publica 08:00–10:00 y 14:00–16:00 (bloques 33, 34) | 201; listado devuelve fecha/inicio/fin reales |
| Disponibilidad 60 min | 08:00, 08:30, 09:00, 14:00, 14:30, 15:00 (sin franjas entre 10:00 y 14:00) |
| USER general 08:00 | 201 `APPROVED` (cita 4), 1 slot, historial `SYSTEM` |
| USER especializada 09:00 y 14:00 | 201 `REQUESTED` (citas 5, 6), 2 slots c/u, historial `USER` |
| Reserva repetida 14:00 y solapada 14:30 | 409, sin duplicado |
| USER / PROFESSIONAL deciden; USER publica bloque | 403 |
| ADMIN rechaza sin motivo | 400 |
| ADMIN aprueba 5 / rechaza 6 con motivo | 200 `APPROVED` (2 slots conservados) / `REJECTED` (slots liberados), historial `ADMIN` |
| Nueva decisión sobre la cita 5 | 409 |
| Disponibilidad posterior | 14:00, 14:30, 15:00 vuelven a ofrecerse |

Consultas de lectura usadas: `appointments` ⨝ `appointment_statuses`; `professional_slots` agrupado por `appointment_id`; `appointment_status_history` ordenado por `id`.

## Pendiente

- Recorrido **visual** web con los tres roles: el agente no introduce contraseñas en formularios de login; debe ejecutarlo una persona (web en `http://localhost:5174` con `FRONTEND_ORIGIN` igual, o 5173 cuando el contenedor web tenga dependencias Linux).
- HU-019 (edición/eliminación de bloques) y restricciones de HU-018 CA-02 / aislamiento HU-020 CA-03 no se reverificaron en este corte.

---

## Recorrido web con los tres roles — 2026-09-29

Autorización del usuario para iniciar sesión con cuentas sintéticas en local (Wiki `preferences.md`, 2026-09-29). Web en contenedor `citas-web-dev` (`http://localhost:5173`, dependencias Linux en `/opt/web`), API `:8080` perfil `local`. Cuentas `s3web-{user,admin,prof}-0929@example.test` (USER registrado desde el formulario web; ADMIN por `INSERT` en `user_roles`; PROFESSIONAL 10 creado por ADMIN con especialidad 60 min id 15 y sede 2). Fecha de agenda 2026-10-06.

| Paso (UI) | Resultado |
|---|---|
| PROFESSIONAL publica 08–10, 14–16 y 17–18 | Listado con nombre de sede, fecha, inicio y fin |
| PROFESSIONAL elimina 17–18 | Desaparece del listado |
| PROFESSIONAL edita bloque libre 2026-10-07 10–11 → 10–12 | Guardado y reflejado (nueva acción "Editar") |
| PROFESSIONAL edita bloque con citas | Mensaje de conflicto; bloque sin cambios (409) |
| USER: sede → Medicina General → fecha futura → profesional → 08:00 | "Cita aprobada", cita 7 `APPROVED` |
| USER: especialidad 60 min 09:00 | "Solicitud registrada", cita 8 `REQUESTED`; franjas ofrecidas sin 08:00 ni huecos entre bloques |
| USER: reabre modal y elige 09:00 ya retenida | Alerta "La franja seleccionada dejó de estar disponible", vuelve a franjas y refresca |
| USER: especialidad 14:00 | Cita 9 `REQUESTED` |
| ADMIN: bandeja `pending-specialized`, aprueba 8 | Desaparece de la bandeja |
| ADMIN: rechazo sin motivo | Bloqueado en formulario |
| ADMIN: rechaza 9 con motivo | Desaparece de la bandeja |

MySQL (lectura): cita 7 `APPROVED` 1 slot `SYSTEM`; cita 8 `APPROVED` 2 slots `USER`→`ADMIN`; cita 9 `REJECTED` 0 slots `USER`→`ADMIN` con motivo. Disponibilidad 60 min posterior: 14:00, 14:30, 15:00.

REST complementario: HU-019 editar/eliminar bloque con citas → 409/409, bloque libre → 200/204; bloque ajeno → 404/404. HU-018 pasado → 400, solapado → 409, no múltiplo de 30 → 400, fin ≤ inicio → 400, sede no asignada → 409. HU-020 listado de bloques como USER o ADMIN → 403; el profesional solo ve sus bloques.

### Defectos UI hallados en el recorrido y corregidos

1. Diálogo de confirmación (`App.tsx`) mostraba `2026-10-06T08:00:00`; ahora `2026-10-06 · 08:00` y estado.
2. "Mis citas" no se refrescaba tras reservar; `App` incrementa una versión que recarga `UserHome`.
3. Al reabrir el modal con los mismos filtros se ofrecían franjas ya tomadas; ahora reconsulta al abrir (prueba nueva).
4. HU-019 sin edición en la web; se añadió "Editar" reutilizando el formulario y `PATCH` (prueba de cliente nueva).

### Suites finales

Maven 22/22 (`compose.test.yml`), Vitest 23/23, `lint` y `build` OK.

### Operación local

- API: `docker compose exec -d -e SPRING_PROFILES_ACTIVE=local citas-api-dev sh -c 'mvn -q spring-boot:run > /tmp/api.log 2>&1'`.
- Web: `docker compose exec -d citas-web-dev sh -c 'cd /opt/web && npx vite --port=5173 --host=0.0.0.0 > /tmp/web.log 2>&1'`. Si el contenedor se recrea: `mkdir -p /opt/web && cd /opt/web && cp /workspace/package*.json . && npm ci`, enlazar `src`, `index.html`, `tsconfig.json`, `.env` desde `/workspace` y crear `vite.config.mjs` con `resolve.preserveSymlinks: true`. No modifica `docker-compose.yml` ni `node_modules` del host.
