package co.com.fcv.training.citas;

import co.com.fcv.training.citas.application.SchedulingFailure;
import co.com.fcv.training.citas.application.SchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Cierre del núcleo de agendamiento (HU-018 a HU-024, HU-033) contra MySQL real. */
@SpringBootTest
@Testcontainers
class SchedulingClosureIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl); r.add("spring.datasource.username", MYSQL::getUsername); r.add("spring.datasource.password", MYSQL::getPassword);
        r.add("app.jwt.access-secret", () -> "a".repeat(40)); r.add("app.jwt.refresh-secret", () -> "b".repeat(40)); r.add("app.cookie.secure", () -> true); r.add("app.cookie.same-site", () -> "None");
    }
    @Autowired SchedulingService scheduling; @Autowired JdbcTemplate jdbc;

    record Fixture(Long professional, Long owner, Long location, Long general, Long specialty, LocalDate date, Long patient, Long admin) {}

    private Fixture fixture(int daysAhead) {
        String s = UUID.randomUUID().toString().substring(0, 8);
        Long professional = scheduling.createProfessional("Sint", "Pro", "CC", "CP" + s, "cp-" + s + "@example.test", "300", "hash", "CP" + s, "LIC" + s);
        Long owner = jdbc.queryForObject("select user_id from professionals where id=?", Long.class, professional);
        Long location = jdbc.queryForObject("select id from locations where active=true order by id limit 1", Long.class);
        Long general = jdbc.queryForObject("select id from specialties where code='MEDICINA_GENERAL'", Long.class);
        Long specialty = scheduling.createSpecialty("CE" + s, "Especialidad " + s, 60, false).id();
        scheduling.setProfessionalSpecialties(professional, List.of(general, specialty), general);
        scheduling.setProfessionalLocations(professional, List.of(location));
        LocalDate date = LocalDate.now().plusDays(daysAhead);
        scheduling.createBlock(owner, location, date, LocalTime.of(8, 0), LocalTime.of(10, 0));
        scheduling.createBlock(owner, location, date, LocalTime.of(14, 0), LocalTime.of(15, 0));
        return new Fixture(professional, owner, location, general, specialty, date, user("cu-" + s), user("ca-" + s));
    }
    private Long user(String key) {
        jdbc.update("insert into users(first_name,last_name,document_type,document_number,email,phone,password_hash,active,email_verified) values ('Test','User','CC',?,?,'300','hash',true,false)", key, key + "@example.test");
        return jdbc.queryForObject("select id from users where email=?", Long.class, key + "@example.test");
    }
    private LocalDateTime at(Fixture f, int h, int m) { return LocalDateTime.of(f.date(), LocalTime.of(h, m)); }
    private int slotsOf(Long appointment) { return jdbc.queryForObject("select count(*) from professional_slots where appointment_id=?", Integer.class, appointment); }
    private List<String> sources(Long appointment) { return jdbc.queryForList("select change_source from appointment_status_history where appointment_id=? order by id", String.class, appointment); }
    private boolean isConflict(Throwable t) { return t instanceof SchedulingFailure sf && sf.kind() == SchedulingFailure.Kind.CONFLICT; }

    @Test void availabilityOffersThirtyAndSixtyMinuteSlotsWithoutGapsBetweenBlocks() {
        Fixture f = fixture(4);
        var general = scheduling.availability(f.location(), f.general(), f.professional(), f.date());
        assertThat(general).hasSize(6).allMatch(a -> Duration.between(a.startAt(), a.endAt()).toMinutes() == 30);
        var special = scheduling.availability(f.location(), f.specialty(), f.professional(), f.date());
        assertThat(special).extracting(SchedulingService.Available::startAt)
            .containsExactly(at(f, 8, 0), at(f, 8, 30), at(f, 9, 0), at(f, 14, 0));
        assertThat(special).noneMatch(a -> a.startAt().toLocalTime().isAfter(LocalTime.of(9, 0)) && a.startAt().toLocalTime().isBefore(LocalTime.of(14, 0)));
    }

    @Test void generalBookingIsApprovedBySystemAndSpecializedIsRequestedByUser() {
        Fixture f = fixture(5);
        var general = scheduling.reserve(f.patient(), f.professional(), f.location(), f.general(), at(f, 8, 0), "Control");
        assertThat(general.status()).isEqualTo("APPROVED");
        assertThat(slotsOf(general.id())).isEqualTo(1);
        assertThat(sources(general.id())).containsExactly("SYSTEM");
        var special = scheduling.reserve(f.patient(), f.professional(), f.location(), f.specialty(), at(f, 9, 0), "Valoración");
        assertThat(special.status()).isEqualTo("REQUESTED");
        assertThat(special.endAt()).isEqualTo(at(f, 10, 0));
        assertThat(slotsOf(special.id())).isEqualTo(2);
        assertThat(sources(special.id())).containsExactly("USER");
        assertThat(scheduling.pending()).anyMatch(p -> ((Number) p.get("id")).longValue() == special.id());
    }

    @Test void adminApprovalKeepsSlotsAndRejectionReleasesThem() {
        Fixture f = fixture(6);
        var approved = scheduling.reserve(f.patient(), f.professional(), f.location(), f.specialty(), at(f, 8, 0), "A");
        var rejected = scheduling.reserve(f.patient(), f.professional(), f.location(), f.specialty(), at(f, 9, 0), "B");
        assertThatThrownBy(() -> scheduling.decide(f.admin(), rejected.id(), "REJECT", " ")).isInstanceOf(IllegalArgumentException.class);
        assertThat(scheduling.decide(f.admin(), approved.id(), "APPROVE", null).status()).isEqualTo("APPROVED");
        assertThat(scheduling.decide(f.admin(), rejected.id(), "REJECT", "Sin agenda clínica").status()).isEqualTo("REJECTED");
        assertThat(slotsOf(approved.id())).isEqualTo(2);
        assertThat(slotsOf(rejected.id())).isZero();
        assertThat(sources(approved.id())).containsExactly("USER", "ADMIN");
        assertThat(sources(rejected.id())).containsExactly("USER", "ADMIN");
        assertThat(scheduling.availability(f.location(), f.specialty(), f.professional(), f.date())).extracting(SchedulingService.Available::startAt).contains(at(f, 9, 0)).doesNotContain(at(f, 8, 0));
        assertThatThrownBy(() -> scheduling.decide(f.admin(), approved.id(), "REJECT", "Tarde")).matches(this::isConflict);
    }

    @Test void rejectsProfessionalWithoutLocationOrSpecialtyEnabled() {
        Fixture f = fixture(7);
        Long otherLocation = jdbc.queryForObject("select id from locations where active=true and id<>? order by id limit 1", Long.class, f.location());
        Long otherSpecialty = scheduling.createSpecialty("NX" + UUID.randomUUID().toString().substring(0, 6), "No habilitada", 30, false).id();
        assertThatThrownBy(() -> scheduling.reserve(f.patient(), f.professional(), otherLocation, f.general(), at(f, 8, 0), "x")).matches(this::isConflict);
        assertThatThrownBy(() -> scheduling.reserve(f.patient(), f.professional(), f.location(), otherSpecialty, at(f, 8, 0), "x")).matches(this::isConflict);
        assertThatThrownBy(() -> scheduling.reserve(f.patient(), f.professional(), f.location(), f.specialty(), at(f, 9, 30), "cruza fin de bloque")).matches(this::isConflict);
    }

    @Test void slotRetainedByRescheduleRequestCannotBeBooked() {
        Fixture f = fixture(8);
        Long original = scheduling.reserve(f.patient(), f.professional(), f.location(), f.general(), at(f, 8, 0), "Original").id();
        scheduling.requestReschedule(f.patient(), original, f.location(), at(f, 9, 0));
        Long other = user("cr-" + UUID.randomUUID().toString().substring(0, 8));
        assertThatThrownBy(() -> scheduling.reserve(other, f.professional(), f.location(), f.general(), at(f, 9, 0), "Choque")).matches(this::isConflict);
        // Retención pura (sin appointment_id) también debe bloquear la reserva.
        Long request = jdbc.queryForObject("select id from appointment_reschedule_requests where appointment_id=?", Long.class, original);
        jdbc.update("update professional_slots ps join availability_blocks b on b.id=ps.availability_block_id set ps.reschedule_request_id=? where b.professional_id=? and ps.start_at=?", request, f.professional(), at(f, 9, 30));
        assertThatThrownBy(() -> scheduling.reserve(other, f.professional(), f.location(), f.general(), at(f, 9, 30), "Choque")).matches(this::isConflict);
        assertThat(jdbc.queryForObject("select count(*) from appointments where patient_user_id=?", Integer.class, other)).isZero();
    }

    @Test void concurrentBookingsOfSameSlotProduceExactlyOneAppointment() throws Exception {
        Fixture f = fixture(9);
        Long second = user("cc-" + UUID.randomUUID().toString().substring(0, 8));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Long>> results = new ArrayList<>();
            for (Long patient : List.of(f.patient(), second)) results.add(pool.submit(() -> { start.await(); return scheduling.reserve(patient, f.professional(), f.location(), f.specialty(), at(f, 8, 0), "Concurrente").id(); }));
            start.countDown();
            int ok = 0, conflicts = 0;
            for (Future<Long> r : results) {
                try { r.get(30, TimeUnit.SECONDS); ok++; } catch (ExecutionException e) { if (isConflict(e.getCause())) conflicts++; else throw e; }
            }
            assertThat(ok).isEqualTo(1); assertThat(conflicts).isEqualTo(1);
        } finally { pool.shutdownNow(); }
        assertThat(jdbc.queryForObject("select count(*) from appointments where professional_id=? and scheduled_start_at=?", Integer.class, f.professional(), at(f, 8, 0))).isEqualTo(1);
    }
}
