package com.exam.parking.service;

import com.exam.parking.entity.*;
import com.exam.parking.enumeration.*;
import com.exam.parking.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            UtilisateurRepository utilisateurRepository,
            VehiculeRepository vehiculeRepository,
            PlaceRepository placeRepository) {

        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.placeRepository = placeRepository;
    }

    public Reservation reserver(
            Long utilisateurId,
            Long vehiculeId,
            Long placeId,
            LocalDateTime debut,
            LocalDateTime fin) {

        if (debut == null || fin == null
                || !debut.isBefore(fin)
                || debut.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "La période de réservation est invalide."
            );
        }

        Utilisateur utilisateur = utilisateurRepository
                .findById(utilisateurId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Utilisateur introuvable."
                    )
                );

        if (!utilisateur.isActif()) {
            throw new IllegalStateException(
                "Le compte utilisateur est désactivé."
            );
        }

        Vehicule vehicule = vehiculeRepository
                .findById(vehiculeId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Véhicule introuvable."
                    )
                );

        if (vehicule.getProprietaire() == null
                || !vehicule.getProprietaire()
                    .getId().equals(utilisateurId)) {
            throw new IllegalStateException(
                "Ce véhicule n'appartient pas à l'utilisateur."
            );
        }

        // Verrouiller la place avant de vérifier les conflits.
        Place place = placeRepository
                .findByIdForUpdate(placeId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Place introuvable."
                    )
                );

        if (place.getStatut() == StatutPlace.MAINTENANCE
                || place.getStatut() == StatutPlace.HORS_SERVICE) {
            throw new IllegalStateException(
                "Cette place est indisponible."
            );
        }

        boolean conflit = reservationRepository
                .existsByPlace_IdAndStatutInAndDateDebutLessThanAndDateFinGreaterThan(
                    placeId,
                    List.of(
                        StatutReservation.EN_ATTENTE,
                        StatutReservation.CONFIRMEE
                    ),
                    fin,
                    debut
                );

        if (conflit) {
            throw new IllegalStateException(
                "Cette place est déjà réservée sur cette période."
            );
        }

        Reservation reservation = new Reservation();

        reservation.setUtilisateur(utilisateur);
        reservation.setVehicule(vehicule);
        reservation.setPlace(place);
        reservation.setDateDebut(debut);
        reservation.setDateFin(fin);
        reservation.setDateCreation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> lister() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation rechercherParId(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Réservation introuvable : " + id
                    )
                );
    }

    @Transactional(readOnly = true)
    public List<Reservation> listerParUtilisateur(Long utilisateurId) {
        return reservationRepository
                .findByUtilisateur_Id(utilisateurId);
    }

    public Reservation confirmer(Long id) {

        Reservation reservation = rechercherParId(id);

        if (reservation.getStatut()
                != StatutReservation.EN_ATTENTE) {
            throw new IllegalStateException(
                "Seule une réservation en attente peut être confirmée."
            );
        }

        if (!reservation.getDateFin()
                .isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                "Cette réservation est expirée."
            );
        }

        reservation.setStatut(StatutReservation.CONFIRMEE);

        return reservationRepository.save(reservation);
    }

    public Reservation annuler(Long id) {

        Reservation reservation = rechercherParId(id);

        if (reservation.getStatut() == StatutReservation.ANNULEE
                || reservation.getStatut() == StatutReservation.EXPIREE) {
            throw new IllegalStateException(
                "Cette réservation ne peut plus être annulée."
            );
        }

        reservation.setStatut(StatutReservation.ANNULEE);

        return reservationRepository.save(reservation);
    }



	@Transactional(readOnly = true)
	public List<Place> placesDisponibles(
	        LocalDateTime debut,
	        LocalDateTime fin) {

		LocalDateTime maintenant = LocalDateTime.now();

		System.out.println("===== DIAGNOSTIC DES DATES =====");
		System.out.println("Date actuelle Java : " + maintenant);
		System.out.println("Date début reçue : " + debut);
		System.out.println("Date fin reçue : " + fin);
		System.out.println("Fuseau horaire Java : "
		        + java.time.ZoneId.systemDefault());

		if (debut != null && fin != null) {
		    System.out.println("Début avant fin : "
		            + debut.isBefore(fin));

		    System.out.println("Début dans le passé : "
		            + debut.isBefore(maintenant));
		}

	    if (debut == null || fin == null
	            || !debut.isBefore(fin)
	            || debut.isBefore(LocalDateTime.now())) {
	        throw new IllegalArgumentException(
	            "La période sélectionnée est invalide."
	        );
	    }

	    return placeRepository.findAll()
	        .stream()
	        .filter(place ->
	            place.getStatut() != StatutPlace.MAINTENANCE
	            && place.getStatut() != StatutPlace.HORS_SERVICE
	        )
	        .filter(place ->
	            !reservationRepository
	                .existsByPlace_IdAndStatutInAndDateDebutLessThanAndDateFinGreaterThan(
	                    place.getId(),
	                    List.of(
	                        StatutReservation.EN_ATTENTE,
	                        StatutReservation.CONFIRMEE
	                    ),
	                    fin,
	                    debut
	                )
	        )
	        .toList();
	}

	public Reservation annulerPourClient(
	        Long reservationId,
	        Long clientId) {

	    Reservation reservation = rechercherParId(reservationId);

	    if (!reservation.getUtilisateur().getId().equals(clientId)) {
	        throw new org.springframework.security.access.AccessDeniedException(
	                "Cette réservation ne vous appartient pas."
	        );
	    }

	    if (reservation.getStatut() != StatutReservation.EN_ATTENTE
	            && reservation.getStatut() != StatutReservation.CONFIRMEE) {
	        throw new IllegalStateException(
	                "Cette réservation ne peut plus être annulée."
	        );
	    }

	    if (!reservation.getDateDebut().isAfter(LocalDateTime.now())) {
	        throw new IllegalStateException(
	                "Une réservation déjà commencée ne peut pas être annulée depuis l'espace client."
	        );
	    }

	    reservation.setStatut(StatutReservation.ANNULEE);

	    return reservationRepository.save(reservation);
	}

}