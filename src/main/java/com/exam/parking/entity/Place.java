package com.exam.parking.entity;

import com.exam.parking.enumeration.StatutPlace;

import jakarta.persistence.*;

@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPlace statut = StatutPlace.LIBRE;

    // Relation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_place_id", nullable = false)
    private TypePlace type;

    // Constructeurs

    public Place() {
    }

    public Place(
            String numero,
            String zone,
            StatutPlace statut,
            TypePlace type) {

        this.numero = numero;
        this.zone = zone;
        this.statut = statut;
        this.type = type;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public String getZone() {
        return zone;
    }

    public StatutPlace getStatut() {
        return statut;
    }

    public TypePlace getType() {
        return type;
    }

    // Setters

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setStatut(StatutPlace statut) {
        this.statut = statut;
    }

    public void setType(TypePlace type) {
        this.type = type;
    }

    // Méthode métier

    public boolean estLibreMaintenant() {

        return statut == StatutPlace.LIBRE;
    }
}