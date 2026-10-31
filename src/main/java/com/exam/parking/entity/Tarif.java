package com.exam.parking.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tarifs")
public class Tarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_place_id", nullable = false)
    private TypePlace typePlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_client_id")
    private CategorieClient categorieClient;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal prixHoraire;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixJournalier;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private boolean actif = true;

    // Constructeurs

    public Tarif() {
    }

    public Tarif(
            TypePlace typePlace,
            CategorieClient categorieClient,
            BigDecimal prixHoraire,
            BigDecimal prixJournalier,
            LocalTime heureDebut,
            LocalTime heureFin) {

        this.typePlace = typePlace;
        this.categorieClient = categorieClient;
        this.prixHoraire = prixHoraire;
        this.prixJournalier = prixJournalier;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.actif = true;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public TypePlace getTypePlace() {
        return typePlace;
    }

    public CategorieClient getCategorieClient() {
        return categorieClient;
    }

    public BigDecimal getPrixHoraire() {
        return prixHoraire;
    }

    public BigDecimal getPrixJournalier() {
        return prixJournalier;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public boolean isActif() {
        return actif;
    }

    // Setters

    public void setTypePlace(TypePlace typePlace) {
        this.typePlace = typePlace;
    }

    public void setCategorieClient(
            CategorieClient categorieClient) {

        this.categorieClient = categorieClient;
    }

    public void setPrixHoraire(BigDecimal prixHoraire) {
        this.prixHoraire = prixHoraire;
    }

    public void setPrixJournalier(BigDecimal prixJournalier) {
        this.prixJournalier = prixJournalier;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    // Méthode métier

    public boolean estApplicable(LocalDateTime date) {

        if (!actif || date == null) {
            return false;
        }

        if (heureDebut == null || heureFin == null) {
            return true;
        }

        LocalTime heure = date.toLocalTime();

        if (heureDebut.equals(heureFin)) {
            return true;
        }

        if (heureDebut.isBefore(heureFin)) {

            return !heure.isBefore(heureDebut)
                    && heure.isBefore(heureFin);
        }

        return !heure.isBefore(heureDebut)
                || heure.isBefore(heureFin);
    }

}