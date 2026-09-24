# Log de operaciones

| Fecha | Operación | Fuentes/páginas | Resultado |
|---|---|---|---|
| 2026-09-17 | INGEST inicial | SRC-PRD-001, SRC-TECH-001, SRC-DB-001, SRC-TRACE-001 | Estructura y páginas iniciales creadas tras aprobación del usuario |
| 2026-09-17 | DECISIÓN | HU-001 a HU-007; `contracts.md`, `decisions.md` | Contrato y corte backend de identidad aprobados; HU-033 conserva integración web |
| 2026-09-17 | LEARN/LINT | Flyway V1, pruebas MySQL 8.4; `data-integrity.md`, `traceability.md` | Corte 3FN y evidencia backend añadidos; RAW intacto, sin nuevos enlaces estructurales |
| 2026-09-22 | LEARN/DECISIÓN | `docs/FCV Dev/subagents/`, orquestador, `index.md`, `subagents.md`, arquitectura, decisiones, riesgos y trazabilidad | Ocho subagentes versionados; protocolo de delegación y nueva ubicación documental registrados; frontend React/Vite reconocido como trabajo pendiente de verificación |
| 2026-09-22 | LINT | Catálogo de subagentes y LLM Wiki | Enlaces Markdown relativos verificados; referencias operativas del orquestador actualizadas; se conserva como riesgo explícito la allowlist histórica de la Skill Scrum |
| 2026-09-22 | LEARN/DECISIÓN | HU-033, `contracts.md`, `traceability.md` | Corte React de autenticación integrado y validado contra API/MySQL; CORS explícito y compatibilidad Flyway con el esquema 3FN existente documentados; HU-033 queda en progreso. |
| 2026-09-22 | LINT | `data-integrity.md`, HU-005 a HU-007 | Evidencia actualizada a 9/9 pruebas Maven y modelo persistente `refresh_tokens`; integración web USER confirmada sin cerrar HU posteriores. |
| 2026-09-22 | DECISIÓN | HU-001, HU-002, HU-011, HU-014 a HU-024 y `contracts.md` | Cierre S2 confirmado para fundación y modelo 3FN; corte S3 aprobado con afiliación opcional, agenda real y contrato REST compartido. |
| 2026-09-23 | DECISIÓN/LEARN | S4, `contracts.md`, migración V3 y evidencia | Contrato S4 aprobado: perfil con teléfono editable, buzón local controlado y preparación n8n; Flyway V1-V3 verificado con MySQL Testcontainers. |
| 2026-09-24 | DECISIÓN/LEARN | S5, `contracts.md`, `decisions.md`, HU-035 | Swagger y adaptador webhook n8n validados localmente (compilación, `/v3/api-docs` 200, Swagger UI 200, rutas protegidas 401; frontend lint/test/build OK). Pendiente: ejecución de las 2 pruebas Testcontainers y prueba real contra n8n Cloud; HU-035 en curso. |
