package com.exam.parking.entity;

import com.exam.parking.enumeration.StatutReservation;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut =
            StatutReservation.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime dateCreation =
            LocalDateTime.now();

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // Constructeurs

    public Reservation() {
    }

    public Reservation(
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            Utilisateur utilisateur,
            Vehicule vehicule,
            Place place) {

        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.utilisateur = utilisateur;
        this.vehicule = vehicule;
        this.place = place;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public Place getPlace() {
        return place;
    }

    // Setters

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public void setDateCreation(
            LocalDateTime dateCreation) {

        this.dateCreation = dateCreation;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    // Méthodes métier

    public boolean estActive() {

        return statut == StatutReservation.CONFIRMEE;
    }

    public void annuler() {

        if (statut == StatutReservation.ANNULEE) {
            throw new IllegalStateException(
                "Cette réservation est déjà annulée."
            );
        }

        this.statut = StatutReservation.ANNULEE;
    }

    public boolean chevauche(Reservation autre) {

        if (autre == null) {
            return false;
        }

        return dateDebut.isBefore(autre.getDateFin())
                && dateFin.isAfter(autre.getDateDebut());
    }
}