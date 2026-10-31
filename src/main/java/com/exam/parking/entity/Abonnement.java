package com.exam.parking.entity;

import com.exam.parking.enumeration.StatutAbonnement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "abonnements")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAbonnement statut = StatutAbonnement.ACTIF;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_abonnement_id", nullable = false)
    private TypeAbonnement type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Constructeurs

    public Abonnement() {
    }

    public Abonnement(
            LocalDate dateDebut,
            LocalDate dateFin,
            StatutAbonnement statut,
            BigDecimal prix,
            TypeAbonnement type,
            Utilisateur utilisateur) {

        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.prix = prix;
        this.type = type;
        this.utilisateur = utilisateur;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public StatutAbonnement getStatut() {
        return statut;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public TypeAbonnement getType() {
        return type;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    // Setters

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public void setStatut(StatutAbonnement statut) {
        this.statut = statut;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }


    public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public void setType(TypeAbonnement type) {
        this.type = type;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    // Méthodes métier

    public boolean estValide(LocalDate date) {

        if (date == null ||
            dateDebut == null ||
            dateFin == null) {
            return false;
        }

        return statut == StatutAbonnement.ACTIF
                && !date.isBefore(dateDebut)
                && date.isBefore(dateFin);
    }

    public void annuler() {
        this.statut = StatutAbonnement.ANNULE;
    }
}