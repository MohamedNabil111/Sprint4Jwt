package com.bolla.sprint4hags.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "username cannot be blank.")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters.")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
