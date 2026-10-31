package com.exam.parking.entity;


import com.exam.parking.enumeration.CategorieClientType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "categories_client")
public class CategorieClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private CategorieClientType nom;

    private String description;

    @Column(
        nullable = false,
        precision = 5,
        scale = 4
    )
    private BigDecimal coefficientRemise = BigDecimal.ZERO;

    private boolean avantage;

    // Constructeurs

    public CategorieClient() {
    }

    public CategorieClient(
            CategorieClientType nom,
            String description,
            BigDecimal coefficientRemise,
            boolean avantage) {

        this.nom = nom;
        this.description = description;
        this.coefficientRemise = coefficientRemise;
        this.avantage = avantage;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public CategorieClientType getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getCoefficientRemise() {
        return coefficientRemise;
    }

    public boolean isAvantage() {
        return avantage;
    }

    // Setters

    public void setNom(CategorieClientType nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoefficientRemise(
            BigDecimal coefficientRemise) {

        this.coefficientRemise = coefficientRemise;
    }

    public void setAvantage(boolean avantage) {
        this.avantage = avantage;
    }
}
