package com.dealkartbd.backend_app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String registrationNumber;
    private String address;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();
}
