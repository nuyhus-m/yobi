package com.S209.yobi.clients.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id", nullable = false)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @NotNull
    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Integer gender;

    @NotNull
    @Column(name = "height", nullable = false)
    private Double height;

    @NotNull
    @Column(name = "weight", nullable = false)
    private Double weight;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Size(max = 100)
    @NotNull
    @Column(name = "address", nullable = false, length = 100)
    private String address;

    public Integer getId() {
        return id;
    }

    public @Size(max = 10) @NotNull String getName() {
        return name;
    }

    public @NotNull LocalDate getBirth() {
        return birth;
    }

    public @NotNull Integer getGender() {
        return gender;
    }

    public @NotNull Double getHeight() {
        return height;
    }

    public @NotNull Double getWeight() {
        return weight;
    }

    public @Size(max = 255) String getImage() {
        return image;
    }

    public @Size(max = 100) @NotNull String getAddress() {
        return address;
    }
}