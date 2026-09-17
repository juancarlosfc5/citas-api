package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

interface UsersJpa extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(String type, String number);
}

interface RolesJpa extends JpaRepository<RoleEntity, Short> {
    Optional<RoleEntity> findByName(String name);
}

interface SessionsJpa extends JpaRepository<SessionEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionEntity s where s.jtiHash = :hash")
    Optional<SessionEntity> lockByJtiHash(@Param("hash") String hash);
}
