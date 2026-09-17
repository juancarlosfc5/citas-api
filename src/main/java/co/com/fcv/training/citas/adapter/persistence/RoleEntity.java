package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
class RoleEntity {
    @Id Short id;
    @Column(nullable = false, length = 32) String name;
    protected RoleEntity() {}
}
