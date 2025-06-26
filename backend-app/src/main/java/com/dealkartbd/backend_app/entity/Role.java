package com.dealkartbd.backend_app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;

    public enum RoleName {
        ADMIN,
        EMPLOYEE,
        COMPANY_OWNER,
        COMPANY_EMPLOYEE,
        SUPER_ADMIN,
        CUSTOMER,
        VENDOR
    }
}
