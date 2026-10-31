package com.exam.parking.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(
        nullable = false,
        unique = true,
        length = 150
    )
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    @Column(nullable = false)
    private boolean actif = true;

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_client_id")
    private CategorieClient categorie;

    @OneToMany(mappedBy = "proprietaire")
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<Abonnement> abonnements = new ArrayList<>();

    // Constructeurs

    public Utilisateur() {
    }

    public Utilisateur(
            String nom,
            String prenom,
            String email,
            String motDePasse,
            String telephone,
            Role role,
            CategorieClient categorie) {

        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.categorie = categorie;
        this.actif = true;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public boolean isActif() {
        return actif;
    }

    public Role getRole() {
        return role;
    }

    public CategorieClient getCategorie() {
        return categorie;
    }

    public List<Vehicule> getVehicules() {
        return vehicules;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public List<Abonnement> getAbonnements() {
        return abonnements;
    }

    // Setters

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setCategorie(CategorieClient categorie) {
        this.categorie = categorie;
    }

    public void setVehicules(List<Vehicule> vehicules) {
        this.vehicules = vehicules;
    }

    public void setReservations(
            List<Reservation> reservations) {

        this.reservations = reservations;
    }

    public void setAbonnements(
            List<Abonnement> abonnements) {

        this.abonnements = abonnements;
    }

    // Méthodes métier

    public boolean estAbonne() {

        return abonnements.stream()
                .anyMatch(a ->
                    a.estValide(java.time.LocalDate.now())
                );
    }
}