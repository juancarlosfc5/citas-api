package co.com.fcv.training.citas.adapter.events;

import co.com.fcv.training.citas.application.Ports;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class N8nWebhookAppointmentEventsTest {
    private HttpServer server;

    @AfterEach void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test void sendsOnlyAuthorizedEventsWithBearerAndMinimalPayload() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        AtomicInteger deliveries = new AtomicInteger();
        startServer(exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            deliveries.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        N8nWebhookAppointmentEvents events = events();
        UUID eventId = UUID.randomUUID();

        events.publish(event(eventId, "APPROVED", "ADMIN"));
        events.publish(event(UUID.randomUUID(), "APPROVED", "SYSTEM"));
        events.publish(event(UUID.randomUUID(), "COMPLETED", "PROFESSIONAL"));

        assertThat(deliveries).hasValue(1);
        assertThat(authorization).hasValue("Bearer shared-test-token");
        assertThat(body.get()).contains("\"schemaVersion\":\"1.0\"", "\"eventId\":\"" + eventId + "\"",
                "\"eventType\":\"appointment.status.changed\"", "\"appointmentId\":42", "\"status\":\"APPROVED\"")
                .doesNotContain("actorId", "previousStatus");
    }

    @Test void deliveryFailureDoesNotPropagateToTheCommittedAppointment() throws Exception {
        startServer(exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        assertThatCode(() -> events().publish(event(UUID.randomUUID(), "REJECTED", "ADMIN"))).doesNotThrowAnyException();
    }

    @Test void requiresUrlAndTokenWhenEnabled() {
        assertThatThrownBy(() -> new N8nWebhookAppointmentEvents("", "", 100, 100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("N8N_WEBHOOK_URL");
    }

    private void startServer(com.sun.net.httpserver.HttpHandler handler) throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hook", handler);
        server.start();
    }

    private N8nWebhookAppointmentEvents events() {
        return new N8nWebhookAppointmentEvents("http://127.0.0.1:" + server.getAddress().getPort() + "/hook", "shared-test-token", 1000, 1000);
    }

    private Ports.AppointmentStatusChanged event(UUID id, String status, String source) {
        return new Ports.AppointmentStatusChanged(id, 42L, null, status, source, 9L, Instant.parse("2026-09-24T12:00:00Z"));
    }
}
