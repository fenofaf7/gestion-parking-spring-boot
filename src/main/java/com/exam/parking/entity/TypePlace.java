package com.exam.parking.entity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "types_place")
public class TypePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal tarifHoraireBase;

    private boolean reserveeVIP;

    // Constructeurs

    public TypePlace() {
    }

    public TypePlace(
            String nom,
            String description,
            BigDecimal tarifHoraireBase,
            boolean reserveeVIP) {

        this.nom = nom;
        this.description = description;
        this.tarifHoraireBase = tarifHoraireBase;
        this.reserveeVIP = reserveeVIP;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getTarifHoraireBase() {
        return tarifHoraireBase;
    }

    public boolean isReserveeVIP() {
        return reserveeVIP;
    }

    // Setters

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTarifHoraireBase(
            BigDecimal tarifHoraireBase) {

        this.tarifHoraireBase = tarifHoraireBase;
    }

    public void setReserveeVIP(boolean reserveeVIP) {
        this.reserveeVIP = reserveeVIP;
    }
}