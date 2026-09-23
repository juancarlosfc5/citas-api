package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import org.springframework.stereotype.Component;

/** Extension point for n8n/webhook delivery. It intentionally produces no external side effect in S4. */
@Component
class NoOpAppointmentEvents implements Ports.AppointmentEvents {
    @Override public void publish(Ports.AppointmentStatusChanged event) { }
}
