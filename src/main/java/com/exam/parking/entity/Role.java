package com.exam.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nom;

    // Constructeurs

    public Role() {
    }

    public Role(String nom) {
        this.nom = nom;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    // Setters

    public void setNom(String nom) {
        this.nom = nom;
    }
}