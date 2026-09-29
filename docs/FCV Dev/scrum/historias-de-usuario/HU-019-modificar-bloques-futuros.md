---
id: HU-019
tipo: historia-de-usuario
titulo: "Modificar bloques futuros"
estado: Completada
epica: "[[EP-004-disponibilidad-del-profesional]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-018-crear-bloques-de-disponibilidad]]"]
relacionadas: ["[[HU-020-consultar-calendario-de-disponibilidad]]"]
---
# HU-019 — Modificar bloques futuros
## Historia de usuario
**COMO** PROFESSIONAL  
**QUIERO** editar o eliminar mis bloques futuros sin citas comprometidas  
**PARA** mantener mi disponibilidad correcta.
## Contexto y descripción
El PRD limita la modificación/eliminación a bloques futuros sin citas comprometidas.
## Alcance
- Editar/eliminar bloque propio que cumpla las condiciones y recalcular slots coherentes.
## Fuera de alcance
- Alterar pasado, bloque ajeno o bloque con cita comprometida.
## Reglas de negocio
- Ownership, futuro, no solapamiento y protección de citas comprometidas.
## Dependencias y relaciones
- Épica: [[EP-004-disponibilidad-del-profesional]]
- Dependencias: [[HU-018-crear-bloques-de-disponibilidad]].
- Relacionadas: [[HU-020-consultar-calendario-de-disponibilidad]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** modifica disponibilidad sin afectar reservas existentes.
## Tareas de desarrollo
- [x] **T-01 — Detectar compromisos y ownership.** Dificultad: Alto. Consultar reservas/retenciones aplicables.
- [x] **T-02 — Aplicar edición/eliminación segura.** Dificultad: Alto. Revalidar futuro, sede y solapamiento.
- [x] **T-03 — Actualizar calendario y pruebas.** Dificultad: Medio. Reflejar éxito/rechazo y probar protección.
## Criterios de aceptación
### CA-01 — Edición permitida
**Dado** un bloque propio futuro sin citas comprometidas, **cuando** PROFESSIONAL lo edita de forma válida, **entonces** la disponibilidad refleja los nuevos slots.
### CA-02 — Eliminación permitida
**Dado** un bloque propio futuro sin citas comprometidas, **cuando** PROFESSIONAL lo elimina, **entonces** sus slots dejan de ofrecerse.
### CA-03 — Protección de compromisos
**Dado** un bloque pasado, ajeno o con citas comprometidas, **cuando** se intenta editar/eliminar, **entonces** se rechaza y las citas no cambian.
## Definition of Done
- [x] CA-01 a CA-03 probados con reglas de agenda y autorización.
- [x] Persistencia/índices aplicables, calendario cliente y contrato REST verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | [[S3-cierre-agendamiento]] web "Editar" + REST 200 | Bloque libre 10–11 → 10–12. |
| CA-02 | Cumple | [[S3-cierre-agendamiento]] web "Eliminar" + REST 204 | — |
| CA-03 / DoD | Cumple | [[S3-cierre-agendamiento]] web + REST 409; ajeno 404 | Bloque con citas protegido; DoD cumplido. |
## Historial de validación
- 2026-09-17 — HU creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Recorrido web con los tres roles y verificación final: [[S3-cierre-agendamiento]]. Estado `Completada`.
## Notas y decisiones
- “Cita comprometida” se verificará contra estados/retenciones aprobados.
