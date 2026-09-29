---
id: HU-023
tipo: historia-de-usuario
titulo: "Solicitar cita especializada"
estado: Completada
epica: "[[EP-005-busqueda-y-reserva-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-021-buscar-disponibilidad]]"]
relacionadas: ["[[HU-024-resolver-solicitud-especializada]]"]
---
# HU-023 — Solicitar cita especializada
## Historia de usuario
**COMO** USER  
**QUIERO** solicitar una cita especializada con sede, profesional y horario  
**PARA** que ADMIN decida la solicitud conservando la franja.
## Contexto y descripción
La solicitud nace `REQUESTED` y retiene slots para evitar doble reserva.
## Alcance
- Confirmación de selección, retención atómica, estado `REQUESTED` e historial.
## Fuera de alcance
- Aprobación automática, cambio de profesional durante solicitud o resolución ADMIN.
## Reglas de negocio
- Especialidad activa/asociada; franja completa; estado inicial `REQUESTED`; retención evita doble reserva.
## Dependencias y relaciones
- Épica: [[EP-005-busqueda-y-reserva-de-citas]]
- Dependencias: [[HU-021-buscar-disponibilidad]].
- Relacionadas: [[HU-024-resolver-solicitud-especializada]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** reserva provisional, concurrencia, estado y auditoría.
## Tareas de desarrollo
- [x] **T-01 — Definir solicitud especializada.** Dificultad: Medio. Documentar selección y respuesta `REQUESTED`.
- [x] **T-02 — Retener slots atómicamente.** Dificultad: Alto. Revalidar disponibilidad completa al confirmar.
- [x] **T-03 — Integrar flujo/pruebas.** Dificultad: Alto. Cubrir 30/60, ya tomada y auditoría.
## Criterios de aceptación
### CA-01 — Solicitud retenida
**Dado** una especialidad/profesional/franja válidos, **cuando** USER confirma, **entonces** se crea cita `REQUESTED` y los slots quedan retenidos.
### CA-02 — Protección de disponibilidad
**Dado** una franja retenida/ocupada, **cuando** otro USER intenta confirmarla, **entonces** no se crea otra reserva sobre esos slots.
### CA-03 — Historial inicial
**Dado** la solicitud creada, **cuando** se consulta auditoría, **entonces** se registra estado, fuente USER y fecha aplicables.
## Definition of Done
- [x] CA-01 a CA-03 probados incluida concurrencia/persistencia.
- [x] Contrato, cliente, migración/índices aplicables y seguridad de ownership verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | [[S3-cierre-agendamiento]] citas 5/6 | `REQUESTED`, 2 slots retenidos. |
| CA-02 | Cumple | REST 409 en 14:00/14:30 | Sin duplicado. |
| CA-03 / DoD | Cumple | [[S3-cierre-agendamiento]] | Historial `USER`. DoD cumplido con recorrido web 2026-09-29. |
## Historial de validación
- 2026-09-17 — HU creada en estado `Pendiente de aprobación`.
- 2026-09-25 — Cierre S3 del núcleo de agendamiento: evidencia en [[S3-cierre-agendamiento]]. Estado sin cambiar hasta recorrido web visual.
- 2026-09-29 — Recorrido web con los tres roles y verificación final: [[S3-cierre-agendamiento]]. Estado `Completada`.
## Notas y decisiones
- La reserva queda liberada al rechazo mediante [[HU-024-resolver-solicitud-especializada]].
