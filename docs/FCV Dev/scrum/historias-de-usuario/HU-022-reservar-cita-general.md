---
id: HU-022
tipo: historia-de-usuario
titulo: "Reservar cita general"
estado: Completada
epica: "[[EP-005-busqueda-y-reserva-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-021-buscar-disponibilidad]]"]
relacionadas: ["[[HU-025-consultar-mis-citas]]", "[[HU-032-consultar-auditoria-de-estados]]"]
---
# HU-022 — Reservar cita general
## Historia de usuario
**COMO** USER  
**QUIERO** confirmar una cita de Medicina General con un profesional disponible  
**PARA** obtener una cita aprobada sin intervención ADMIN.
## Contexto y descripción
La disponibilidad se debe revalidar en la confirmación para impedir doble reserva.
## Alcance
- Selección válida, confirmación transaccional, `APPROVED`, slots ocupados e historial.
## Fuera de alcance
- Flujo especializada, aprobación ADMIN o asignación automática de profesional no solicitada.
## Reglas de negocio
- General se aprueba automáticamente; ninguna cita ocupa slots reservados/retenidos.
## Dependencias y relaciones
- Épica: [[EP-005-busqueda-y-reserva-de-citas]]
- Dependencias: [[HU-021-buscar-disponibilidad]].
- Relacionadas: [[HU-025-consultar-mis-citas]], [[HU-032-consultar-auditoria-de-estados]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** exige operación atómica de disponibilidad, estado y auditoría.
## Tareas de desarrollo
- [x] **T-01 — Definir comando/resultado de reserva.** Dificultad: Medio. Acordar datos y errores de disponibilidad.
- [x] **T-02 — Aplicar reserva atómica.** Dificultad: Alto. Revalidar slots y persistir cita/estado/historial.
- [x] **T-03 — Integrar confirmación/pruebas.** Dificultad: Alto. Cubrir éxito, carrera y slot ya ocupado.
## Criterios de aceptación
### CA-01 — Aprobación automática
**Dado** Medicina General, profesional y franja aún disponible, **cuando** USER confirma, **entonces** se crea una cita `APPROVED` sin acción ADMIN.
### CA-02 — Sin doble reserva
**Dado** una franja tomada entre búsqueda y confirmación, **cuando** USER confirma, **entonces** se rechaza y no se crea una segunda cita.
### CA-03 — Trazabilidad de estado
**Dado** una reserva exitosa, **cuando** se consulta su historial, **entonces** consta el cambio a `APPROVED` con fuente/fecha aplicables.
## Definition of Done
- [x] CA-01 a CA-03 probados, incluida concurrencia/integración de persistencia relevante.
- [x] Contrato, cliente, migración/índices aplicables y auditoría verificables.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | [[S3-cierre-agendamiento]] cita 4 | `APPROVED` sin ADMIN. |
| CA-02 | Cumple | `concurrentBookings…`, `slotRetained…`; REST 409 | Corregida doble reserva concurrente. |
| CA-03 / DoD | Cumple | [[S3-cierre-agendamiento]] | Historial `SYSTEM`. DoD cumplido con recorrido web 2026-09-29. |
## Historial de validación
- 2026-09-17 — HU creada en estado `Pendiente de aprobación`.
- 2026-09-25 — Cierre S3 del núcleo de agendamiento: evidencia en [[S3-cierre-agendamiento]]. Estado sin cambiar hasta recorrido web visual.
- 2026-09-29 — Recorrido web con los tres roles y verificación final: [[S3-cierre-agendamiento]]. Estado `Completada`.
## Notas y decisiones
- Medicina General depende de la especialidad/catálogo acordado.
