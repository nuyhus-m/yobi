package com.S209.yobi.domain.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@BatchSize(size = 100)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @NotNull
    @Column(name = "employee_number", nullable = false, unique = true)
    private Integer employeeNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "consent", nullable = true)
    @Builder.Default
    private Boolean consent = false;

    @Size(max = 255)
    @Column(name = "image", nullable = true)
    private String image;
}