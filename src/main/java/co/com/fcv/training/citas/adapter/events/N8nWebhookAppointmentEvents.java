package co.com.fcv.training.citas.adapter.events;

import co.com.fcv.training.citas.application.Ports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

/** Delivers the approved minimal appointment events after the database transaction commits. */
@Component
@ConditionalOnProperty(prefix = "app.n8n.webhook", name = "enabled", havingValue = "true")
class N8nWebhookAppointmentEvents implements Ports.AppointmentEvents {
    private static final Logger log = LoggerFactory.getLogger(N8nWebhookAppointmentEvents.class);
    private final RestClient client;
    private final String webhookUrl;
    private final String bearerToken;

    N8nWebhookAppointmentEvents(@Value("${app.n8n.webhook.url}") String webhookUrl,
                                 @Value("${app.n8n.webhook.bearer-token}") String bearerToken,
                                 @Value("${app.n8n.webhook.connect-timeout-ms}") int connectTimeoutMs,
                                 @Value("${app.n8n.webhook.read-timeout-ms}") int readTimeoutMs) {
        if (webhookUrl == null || webhookUrl.isBlank() || bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalStateException("N8N_WEBHOOK_URL y N8N_WEBHOOK_BEARER_TOKEN son obligatorios cuando N8N_WEBHOOK_ENABLED=true");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.client = RestClient.builder().requestFactory(factory).build();
        this.webhookUrl = webhookUrl;
        this.bearerToken = bearerToken;
    }

    @Override
    public void publish(Ports.AppointmentStatusChanged event) {
        if (!isAuthorizedEvent(event)) return;
        try {
            client.post().uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(new WebhookPayload("1.0", event.eventId(), "appointment.status.changed", event.appointmentId(),
                            event.status(), event.source(), event.occurredAt()))
                    .retrieve().toBodilessEntity();
            log.info("n8n webhook delivered eventId={} appointmentId={} status={}", event.eventId(), event.appointmentId(), event.status());
        } catch (RuntimeException exception) {
            // The state transition has committed. Delivery failure must never alter appointment data.
            log.warn("n8n webhook delivery failed eventId={} appointmentId={} status={} cause={}",
                    event.eventId(), event.appointmentId(), event.status(), exception.getClass().getSimpleName());
        }
    }

    private boolean isAuthorizedEvent(Ports.AppointmentStatusChanged event) {
        return ("ADMIN".equals(event.source()) && ("APPROVED".equals(event.status()) || "REJECTED".equals(event.status())))
                || ("USER".equals(event.source()) && "CANCELLED".equals(event.status()));
    }

    record WebhookPayload(String schemaVersion, UUID eventId, String eventType, Long appointmentId,
                          String status, String source, Instant occurredAt) { }
}
