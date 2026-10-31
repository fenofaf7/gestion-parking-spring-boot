package com.exam.parking.entity;

import com.exam.parking.enumeration.StatutStationnement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "stationnements")
public class Stationnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateEntree;

    private LocalDateTime dateSortie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutStationnement statut =
            StatutStationnement.EN_COURS;

    @Column(name = "prix_horaire_applique",
            nullable = false,
            precision = 12,
            scale = 2)
    private BigDecimal prixHoraireApplique;

    @Column(name = "prix_journalier_applique",
            precision = 12,
            scale = 2)
    private BigDecimal prixJournalierApplique;

    // Relations

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarif_id", nullable = false)
    private Tarif tarif;

    @Column(name = "montant_total", precision = 12, scale = 2)
    private BigDecimal montantTotal;

    // Constructeurs

	public Stationnement() {
    }

    public Stationnement(
            LocalDateTime dateEntree,
            Place place,
            Vehicule vehicule,
            Reservation reservation,
            Tarif tarif) {

        this.dateEntree = dateEntree;
        this.place = place;
        this.vehicule = vehicule;
        this.reservation = reservation;
        this.tarif = tarif;
        this.statut = StatutStationnement.EN_COURS;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateEntree() {
        return dateEntree;
    }

    public LocalDateTime getDateSortie() {
        return dateSortie;
    }

    public StatutStationnement getStatut() {
        return statut;
    }

    public Place getPlace() {
        return place;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Tarif getTarif() {
        return tarif;
    }

    public BigDecimal getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(BigDecimal montantTotal) {
		this.montantTotal = montantTotal;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getPrixHoraireApplique() {
		return prixHoraireApplique;
	}

	public void setPrixHoraireApplique(BigDecimal prixHoraireApplique) {
		this.prixHoraireApplique = prixHoraireApplique;
	}

	public BigDecimal getPrixJournalierApplique() {
		return prixJournalierApplique;
	}

	public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) {
		this.prixJournalierApplique = prixJournalierApplique;
	}

    // Setters

    public void setDateEntree(LocalDateTime dateEntree) {
        this.dateEntree = dateEntree;
    }

    public void setDateSortie(LocalDateTime dateSortie) {
        this.dateSortie = dateSortie;
    }

    public void setStatut(StatutStationnement statut) {
        this.statut = statut;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setTarif(Tarif tarif) {
        this.tarif = tarif;
    }

    // Méthodes métier

    public Duration calculerDuree() {

        if (dateEntree == null) {
            throw new IllegalStateException(
                "La date d'entrée est obligatoire."
            );
        }

        LocalDateTime fin =
                dateSortie != null
                ? dateSortie
                : LocalDateTime.now();

        if (fin.isBefore(dateEntree)) {
            throw new IllegalStateException(
                "La sortie ne peut pas précéder l'entrée."
            );
        }

        return Duration.between(dateEntree, fin);
    }

    public void cloturer(LocalDateTime sortie) {

        if (statut != StatutStationnement.EN_COURS) {
            throw new IllegalStateException(
                "Ce stationnement n'est pas en cours."
            );
        }

        if (sortie == null ||
            sortie.isBefore(dateEntree)) {

            throw new IllegalArgumentException(
                "Date de sortie invalide."
            );
        }

        this.dateSortie = sortie;
        this.statut = StatutStationnement.TERMINE;
    }
}