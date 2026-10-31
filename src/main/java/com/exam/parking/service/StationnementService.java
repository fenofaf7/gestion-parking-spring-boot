package com.exam.parking.service;

import com.exam.parking.entity.*;
import com.exam.parking.enumeration.*;
import com.exam.parking.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class StationnementService {

    private final StationnementRepository stationnementRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;
    private final TarifService tarifService;

    public StationnementService(
            StationnementRepository stationnementRepository,
            VehiculeRepository vehiculeRepository,
            PlaceRepository placeRepository,
            ReservationRepository reservationRepository,
            TarifService tarifService) {

        this.stationnementRepository = stationnementRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.placeRepository = placeRepository;
        this.reservationRepository = reservationRepository;
        this.tarifService = tarifService;
    }

    // Enregistrer une entrée
    public Stationnement enregistrerEntree(
            Long vehiculeId,
            Long placeId,
            Long reservationId) {

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() ->
                    new NoSuchElementException("Véhicule introuvable.")
                );

        // Verrouiller la place pendant l'opération
        Place place = placeRepository.findByIdForUpdate(placeId)
                .orElseThrow(() ->
                    new NoSuchElementException("Place introuvable.")
                );

        if (place.getStatut() != StatutPlace.LIBRE) {
            throw new IllegalStateException(
                "Cette place n'est pas libre."
            );
        }

        if (stationnementRepository.existsByVehicule_IdAndStatut(
                vehiculeId, StatutStationnement.EN_COURS)) {
            throw new IllegalStateException(
                "Ce véhicule possède déjà un stationnement en cours."
            );
        }

        Reservation reservation = null;

        if (reservationId != null) {
            reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() ->
                        new NoSuchElementException(
                            "Réservation introuvable."
                        )
                    );

            if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
                throw new IllegalStateException(
                    "La réservation n'est pas confirmée."
                );
            }

            if (!reservation.getVehicule().getId().equals(vehiculeId)
                    || !reservation.getPlace().getId().equals(placeId)) {
                throw new IllegalStateException(
                    "La réservation ne correspond pas au véhicule ou à la place."
                );
            }


			LocalDateTime maintenant = LocalDateTime.now();

			if (maintenant.isBefore(reservation.getDateDebut())) {
			    throw new IllegalStateException(
			        "L'entrée est trop tôt. La réservation commence le "
			        + reservation.getDateDebut()
			    );
			}

			if (!maintenant.isBefore(reservation.getDateFin())) {
			    throw new IllegalStateException(
			        "La réservation est expirée depuis le "
			        + reservation.getDateFin()
			    );
			}


            if (stationnementRepository
                    .findByReservation_Id(reservationId).isPresent()) {
                throw new IllegalStateException(
                    "Cette réservation a déjà été utilisée."
                );
            }
        } else {
            // Sans réservation, refuser une place réservée maintenant.
            LocalDateTime maintenant = LocalDateTime.now();

            boolean reservee = reservationRepository
                    .existsByPlace_IdAndStatutInAndDateDebutLessThanAndDateFinGreaterThan(
                        placeId,
                        List.of(
                            StatutReservation.EN_ATTENTE,
                            StatutReservation.CONFIRMEE
                        ),
                        maintenant.plusNanos(1),
                        maintenant
                    );

            if (reservee) {
                throw new IllegalStateException(
                    "Cette place est actuellement réservée."
                );
            }
        }

        Long categorieId = null;

        if (vehicule.getProprietaire() != null
                && vehicule.getProprietaire().getCategorie() != null) {

            categorieId = vehicule.getProprietaire()
                .getCategorie()
                .getId();
        }

        LocalDateTime dateEntree = LocalDateTime.now();

        Tarif tarif = tarifService.rechercherTarifApplicable(
            place.getType().getId(),
            categorieId,
            dateEntree
        );

        Stationnement stationnement = new Stationnement();

        stationnement.setVehicule(vehicule);
        stationnement.setPlace(place);
        stationnement.setReservation(reservation);
        stationnement.setDateEntree(LocalDateTime.now());
        stationnement.setTarif(tarif);
        stationnement.setTarif(tarif);

        stationnement.setPrixHoraireApplique(
            tarif.getPrixHoraire()
        );

        stationnement.setPrixJournalierApplique(
            tarif.getPrixJournalier()
        );
        stationnement.setStatut(StatutStationnement.EN_COURS);

        place.setStatut(StatutPlace.OCCUPEE);

        stationnementRepository.save(stationnement);
        placeRepository.save(place);

        return stationnement;
    }

    // Enregistrer la sortie
    public Stationnement enregistrerSortie(Long stationnementId) {

        Stationnement stationnement = rechercherParId(stationnementId);

        Place place = placeRepository.findByIdForUpdate(
                stationnement.getPlace().getId()
        ).orElseThrow(() ->
                new NoSuchElementException("Place introuvable.")
        );

        if (stationnement.getStatut() != StatutStationnement.EN_COURS) {
            throw new IllegalStateException(
                "Ce stationnement est déjà terminé."
            );
        }

        LocalDateTime sortie = LocalDateTime.now();

        Long categorieId = null;

        if (stationnement.getVehicule().getProprietaire() != null
                && stationnement.getVehicule()
                    .getProprietaire().getCategorie() != null) {

            categorieId = stationnement.getVehicule()
                    .getProprietaire().getCategorie().getId();
        }


		BigDecimal montantBrut =
		    tarifService.calculerMontantAvecPlafondJournalier(
		        stationnement.getPrixHoraireApplique(),
		        stationnement.getPrixJournalierApplique(),
		        stationnement.getDateEntree(),
		        sortie
		    );

		BigDecimal montant =
		    tarifService.appliquerRemise(
		        montantBrut,
		        categorieId
		);


        stationnement.setDateSortie(sortie);
        stationnement.setMontantTotal(montant);
        stationnement.setStatut(StatutStationnement.TERMINE);

        place.setStatut(StatutPlace.LIBRE);

        stationnementRepository.save(stationnement);
        placeRepository.save(place);

        return stationnement;
    }

    @Transactional(readOnly = true)
    public Stationnement rechercherParId(Long id) {
        return stationnementRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Stationnement introuvable : " + id
                    )
                );
    }

    @Transactional(readOnly = true)
    public List<Stationnement> lister() {
        return stationnementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Stationnement> listerEnCours() {
        return stationnementRepository
                .findByStatut(StatutStationnement.EN_COURS);
    }
}