package com.exam.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicules")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        unique = true,
        length = 30
    )
    private String immatriculation;

    private String marque;

    private String modele;

    private String couleur;

    // Relation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;

    // Constructeurs

    public Vehicule() {
    }

    public Vehicule(
            String immatriculation,
            String marque,
            String modele,
            String couleur,
            Utilisateur proprietaire) {

        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.couleur = couleur;
        this.proprietaire = proprietaire;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public String getModele() {
        return modele;
    }

    public String getCouleur() {
        return couleur;
    }

    public Utilisateur getProprietaire() {
        return proprietaire;
    }

    // Setters

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.proprietaire = proprietaire;
    }
}
