CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX ix_password_reset_user (user_id), INDEX ix_password_reset_expiry (expires_at)
) ENGINE=InnoDB;
CREATE TABLE reschedule_statuses (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE, name VARCHAR(80) NOT NULL, is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;
INSERT INTO reschedule_statuses(id,code,name,is_terminal) VALUES
 (1,'PENDING','Pendiente',FALSE),(2,'APPROVED','Aprobada',TRUE),(3,'REJECTED','Rechazada',TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name),is_terminal=VALUES(is_terminal);
CREATE TABLE appointment_reschedule_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    requested_location_id SMALLINT UNSIGNED NOT NULL,
    requested_start_at DATETIME NOT NULL, requested_end_at DATETIME NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    requested_by_user_id BIGINT UNSIGNED NOT NULL,
    decided_by_user_id BIGINT UNSIGNED NULL, decision_reason VARCHAR(500) NULL,
    decided_at DATETIME NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (requested_location_id) REFERENCES locations(id),
    FOREIGN KEY (status_id) REFERENCES reschedule_statuses(id),
    FOREIGN KEY (requested_by_user_id) REFERENCES users(id), FOREIGN KEY (decided_by_user_id) REFERENCES users(id),
    INDEX ix_reschedule_inbox(status_id,requested_start_at), INDEX ix_reschedule_appointment(appointment_id)
) ENGINE=InnoDB;
ALTER TABLE professional_slots
    ADD COLUMN reschedule_request_id BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_slots_reschedule FOREIGN KEY (reschedule_request_id) REFERENCES appointment_reschedule_requests(id) ON DELETE SET NULL,
    ADD INDEX ix_slots_reschedule (reschedule_request_id);
