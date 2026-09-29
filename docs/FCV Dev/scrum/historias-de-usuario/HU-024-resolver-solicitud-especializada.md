---
id: HU-024
tipo: historia-de-usuario
titulo: "Resolver solicitud especializada"
estado: Completada
epica: "[[EP-005-busqueda-y-reserva-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-023-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-031-consultar-bandeja-administrativa]]", "[[HU-032-consultar-auditoria-de-estados]]"]
---
# HU-024 — Resolver solicitud especializada
## Historia de usuario
**COMO** ADMIN  
**QUIERO** aprobar o rechazar una cita especializada solicitada  
**PARA** decidir la atención y liberar la franja cuando corresponda.
## Contexto y descripción
El rechazo exige motivo; aprobar cambia a `APPROVED`, rechazar a `REJECTED` y libera slots.
## Alcance
- Decisión ADMIN sobre `REQUESTED`, validación de transición, motivo de rechazo, slots e historial.
## Fuera de alcance
- Decidir citas generales o modificar selección clínica.
## Reglas de negocio
- Rechazo exige motivo; transiciones explícitas; rechazo libera la reserva.
## Dependencias y relaciones
- Épica: [[EP-005-busqueda-y-reserva-de-citas]]
- Dependencias: [[HU-023-solicitar-cita-especializada]].
- Relacionadas: [[HU-031-consultar-bandeja-administrativa]], [[HU-032-consultar-auditoria-de-estados]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** coordina autorización, transición, liberación y auditoría.
## Tareas de desarrollo
- [x] **T-01 — Definir decisión ADMIN.** Dificultad: Medio. Acordar precondición `REQUESTED`, motivo y errores.
- [x] **T-02 — Aplicar transición atómica.** Dificultad: Alto. Aprobar o rechazar/liberar sin estados intermedios.
- [x] **T-03 — Integrar bandeja/pruebas.** Dificultad: Alto. Cubrir rol, motivo obligatorio, slots e historial.
## Criterios de aceptación
### CA-01 — Aprobación
**Dado** una cita `REQUESTED`, **cuando** ADMIN aprueba, **entonces** pasa a `APPROVED` y conserva la reserva.
### CA-02 — Rechazo con motivo
**Dado** una cita `REQUESTED`, **cuando** ADMIN rechaza con motivo, **entonces** pasa a `REJECTED`, registra motivo y libera slots.
### CA-03 — Decisión válida
**Dado** actor no ADMIN, estado distinto de `REQUESTED` o rechazo sin motivo, **cuando** intenta decidir, **entonces** se rechaza sin transición.
## Definition of Done
- [x] CA-01 a CA-03 probados con persistencia/REST, cliente ADMIN y auditoría.
- [x] Transición/liberación atómica y contrato cross-repo verificables.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | [[S3-cierre-agendamiento]] cita 5 | Slots conservados, `ADMIN`. |
| CA-02 | Cumple | [[S3-cierre-agendamiento]] cita 6; 400 sin motivo | Slots liberados y reofrecidos. |
| CA-03 / DoD | Cumple | [[S3-cierre-agendamiento]]; 409 re-decisión; 403 USER/PROF | DoD cumplido con recorrido web 2026-09-29. |
## Historial de validación
- 2026-09-17 — HU creada en estado `Pendiente de aprobación`.
- 2026-09-25 — Cierre S3 del núcleo de agendamiento: evidencia en [[S3-cierre-agendamiento]]. Estado sin cambiar hasta recorrido web visual.
- 2026-09-29 — Recorrido web con los tres roles y verificación final: [[S3-cierre-agendamiento]]. Estado `Completada`.
## Notas y decisiones
- La bandeja se especifica en [[HU-031-consultar-bandeja-administrativa]].
