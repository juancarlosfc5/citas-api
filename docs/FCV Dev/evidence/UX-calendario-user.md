# Evidencia — calendario de agendamiento USER (2026-09-29)

Plan: `PLAN_TRABAJO_CALENDARIO_USER.md` (raíz). Opción A aprobada por el usuario: endpoint aditivo.

## Contrato
`GET /api/v1/availability/days?locationId&specialtyId&from&to` (rol USER) → `[{ "date": "YYYY-MM-DD", "slots": n }]`. Reutiliza la regla de `/availability` (franjas libres y no retenidas; 60 min = pares consecutivos). `400` si `to < from`, `from < hoy` o rango ≥ 62 días. No modifica endpoints existentes.

## Frontend
- `AvailabilityCalendar.tsx`: mes con semana lunes–domingo, días sin horarios deshabilitados, día seleccionado en `bg-blue-600`, fechas como texto (sin zona del navegador), `aria-label` por día.
- `BookAppointmentModal.tsx` en 3 pasos: especialidad (tarjetas) + sede → calendario + horas por profesional → confirmación. Mismos tokens, tarjetas, resumen y pie del diseño previo. `409` vuelve al paso 2 y refresca días y horas.

## Datos semilla (solo desarrollo)
`citas-api/scripts/seed-dev-agenda.sql` + `seed-dev-agenda.ps1` (idempotente, fuera de Flyway): 6 profesionales `SEED-DEV-01..06` (`seed-dev-0N@example.test`, sin login) en HIC/ICV para Medicina General, Cardiología Adulto, Pediatría, Neurología y Ortopedia; bloques lunes–sábado del 2026-09-29 al 2026-10-15 (el 29-sep solo tarde). Resultado: 52 bloques, 350 franjas; segunda ejecución sin duplicados.

## Pruebas
- Maven 23/23 (nuevo `availableDaysListsOnlyDatesWithReservableSlotsAndValidatesRange`).
- Vitest 27/27 (calendario, modal en 3 pasos, cliente `availableDays`), `lint` y `build` OK.
- REST en vivo: rango inválido → 400; Medicina General HIC → 14 días.
- Web como USER: Neurología HIC → calendario Oct con días 1, 2, 6, 8, 13, 15 → 08-oct 15:00 → `REQUESTED` (cita 10, 2 slots, `USER`); Medicina General ICV → 01-oct 14:00 → `APPROVED` (cita 11, 1 slot, `SYSTEM`).

## Observaciones
- `/availability` y `/availability/days` no excluyen franjas de hoy ya pasadas; `reserve` sí las rechaza. Mejora posible fuera de este alcance.
- El mes mostrado se conserva al reabrir el modal.
