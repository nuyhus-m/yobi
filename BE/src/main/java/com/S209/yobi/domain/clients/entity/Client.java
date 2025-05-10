package com.S209.yobi.domain.clients.entity;

import com.S209.yobi.domain.measures.entity.Measure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "clients")
@BatchSize(size = 100)
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

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<Measure> measures = new ArrayList<>();


    public Integer getId() {
        return id;
    }

    public @Size(max = 10) @NotNull String getName() {
        return name;
    }


}