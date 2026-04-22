package com.pm.tracker.model.operational;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User() {}

    public User(String username, Role role) {
        this.username = username;
        this.role     = role;
    }

    public UUID getId()               { return id; }
    public String getUsername()       { return username; }
    public void setUsername(String u) { this.username = u; }
    public Role getRole()             { return role; }
    public void setRole(Role role)    { this.role = role; }
}