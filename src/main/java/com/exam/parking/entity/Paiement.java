package com.exam.parking.entity;

import com.exam.parking.enumeration.ModePaiement;
import com.exam.parking.enumeration.StatutPaiement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal montant;

    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement mode;

	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut =
            StatutPaiement.EN_ATTENTE;

    @Column(unique = true)
    private String reference;

    // Relation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stationnement_id", nullable = false)
    private Stationnement stationnement;

    // Constructeurs

    public Paiement() {
    }

    public Paiement(
            BigDecimal montant,
            ModePaiement mode,
            String reference,
            Stationnement stationnement) {

        this.montant = montant;
        this.mode = mode;
        this.reference = reference;
        this.stationnement = stationnement;
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public ModePaiement getModePaiement() {
        return mode;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public String getReference() {
        return reference;
    }

    public Stationnement getStationnement() {
        return stationnement;
    }

    // Setters

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public void setDatePaiement(
            LocalDateTime datePaiement) {

        this.datePaiement = datePaiement;
    }

    public void setMode(ModePaiement modePaiement) {
        this.mode = modePaiement;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setStationnement(
            Stationnement stationnement) {

        this.stationnement = stationnement;
    }

    // Méthodes métier

    public void confirmer() {

        if (statut != StatutPaiement.EN_ATTENTE) {
            throw new IllegalStateException(
                "Ce paiement ne peut pas être confirmé."
            );
        }

        this.statut = StatutPaiement.CONFIRME;
        this.datePaiement = LocalDateTime.now();
    }

    public void echouer() {

        if (statut != StatutPaiement.EN_ATTENTE) {
            throw new IllegalStateException(
                "Ce paiement ne peut pas être marqué en échec."
            );
        }

        this.statut = StatutPaiement.ECHEC;
    }
}