# Trazabilidad

## HECHO

Las sesiones S2-S6 requieren commits y evidencias específicas. El backend y frontend deben mantener historial trazable; las pruebas y la evidencia cross-repo son parte de la evaluación.

## HECHO — 2026-09-17

Las HU-001 a HU-036 existen en `docs/FCV Dev/scrum/`. HU-001/002/003 tienen avance parcial; HU-004 define por ahora solo el contrato de identidad. La implementación backend de HU-005/006/007 cuenta con `AuthIntegrationTest`, `IdentityTest` y `AuthRequestGuardTest`; la integración web se controla mediante HU-033.

## HECHO — 2026-09-22

El catálogo de ocho subagentes fue versionado en `docs/FCV Dev/subagents/` y enlazado desde el orquestador. `citas-web` contiene trabajo local React/Vite de autenticación y pruebas; no debe declararse completado hasta ejecutar build, typecheck, tests y verificación cross-repo.

## HECHO — 2026-09-22 · Integración de autenticación

El prototipo `citas-web/portal-de-citas.zip` se importó como React/Vite y se integró con HU-005/006/007. La comprobación usa MySQL persistente, CORS explícito, registro/login/refresh/logout reales y pruebas de frontend. HU-033 permanece en progreso porque las pantallas de perfil, agenda y roles posteriores siguen fuera del corte de autenticación.

## HECHO — 2026-09-23 · Corte S4

El backend implementa recuperación local controlada, perfil, EPS/planes, cancelación, reprogramación retenida, agenda/cierre profesional, bandeja administrativa, auditoría y consulta de citas próximas. El frontend React integra los flujos USER, PROFESSIONAL y ADMIN sin fuentes simuladas de citas. Las pruebas Maven con Testcontainers, Vitest, lint y build se ejecutaron contra las migraciones V1-V3. La evidencia operativa está en `docs/FCV Dev/evidence/S4.md`.

## DECISIÓN — 2026-09-25 · Aislamiento de secretos n8n

Se retiró la inyección de variables n8n desde `docker-compose.yml`. El backend obtiene exclusivamente la configuración de WF-002 desde su entorno local `citas-api/.env`, con URL y Bearer propios. Quedan documentados prefijos separados para WF-001 y WF-003, sin activar ni implementar sus callbacks.

## HECHO — 2026-09-25 · Cierre S3 del núcleo de agendamiento

Reverificación de HU-018, HU-020 a HU-024 y HU-033 con evidencia nueva en `docs/FCV Dev/evidence/S3-cierre-agendamiento.md`: Maven 22/22 (Testcontainers MySQL 8.4), Vitest 21/21, lint y build, y recorrido REST + MySQL con los tres roles. Se corrigieron una doble reserva concurrente (lectura no bloqueante de ocupación), la retención por reprogramación en la reserva, el `500` al redecidir, y en web el orden del modal, el endpoint ADMIN y el manejo de hora local. CA con evidencia marcados en cada HU; DoD y estados de HU permanecen abiertos hasta el recorrido web visual. HU-019 no se reverificó.

## HECHO — 2026-09-29 · HU-018 a HU-024 completadas

Recorrido web con USER, PROFESSIONAL y ADMIN sobre API y MySQL locales, más verificación REST de HU-018 CA-02, HU-019 y HU-020 CA-03. HU-018 a HU-024 pasan a `Completada`; HU-033 sigue `En progreso` (pantallas de HU posteriores). Se corrigieron cuatro defectos de UI (formato en confirmación, refresco de "Mis citas", franjas obsoletas al reabrir el modal, edición de bloques ausente). Maven 22/22, Vitest 23/23. Evidencia: `docs/FCV Dev/evidence/S3-cierre-agendamiento.md`.
