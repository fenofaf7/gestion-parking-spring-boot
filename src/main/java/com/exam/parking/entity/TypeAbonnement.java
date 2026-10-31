package com.exam.parking.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "types_abonnement")
public class TypeAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(nullable = false)
    private int duree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private java.time.temporal.ChronoUnit uniteDuree;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal prix;

    private boolean actif = true;

    // Constructeurs

    public TypeAbonnement() {
    }

    public TypeAbonnement(
            String nom,
            String description,
            int duree,
            java.time.temporal.ChronoUnit uniteDuree,
            BigDecimal prix) {

        this.nom = nom;
        this.description = description;
        this.duree = duree;
        this.uniteDuree = uniteDuree;
        this.prix = prix;
        this.actif = true;
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

    public int getDuree() {
        return duree;
    }

    public java.time.temporal.ChronoUnit getUniteDuree() {
        return uniteDuree;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public boolean isActif() {
        return actif;
    }

    // Setters

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public void setUniteDuree(
            java.time.temporal.ChronoUnit uniteDuree) {

        this.uniteDuree = uniteDuree;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}