package com.exam.parking.service;

import com.exam.parking.entity.Place;
import com.exam.parking.entity.TypePlace;

import com.exam.parking.enumeration.StatutPlace;
import com.exam.parking.enumeration.StatutReservation;
import com.exam.parking.enumeration.StatutStationnement;

import com.exam.parking.repository.PlaceRepository;
import com.exam.parking.repository.TypePlaceRepository;
import com.exam.parking.repository.ReservationRepository;
import com.exam.parking.repository.StationnementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TypePlaceRepository typePlaceRepository;
    private final ReservationRepository reservationRepository;
    private final StationnementRepository stationnementRepository;

    public PlaceService(
            PlaceRepository placeRepository,
            TypePlaceRepository typePlaceRepository,
            ReservationRepository reservationRepository,
            StationnementRepository stationnementRepository) {

        this.placeRepository = placeRepository;
        this.typePlaceRepository = typePlaceRepository;
        this.reservationRepository = reservationRepository;
        this.stationnementRepository = stationnementRepository;
    }

    // Ajouter une place
    public Place ajouter(
            String numero,
            String zone,
            Long typePlaceId) {

        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException(
                "Le numéro de place est obligatoire."
            );
        }

        String numeroNormalise = numero.trim().toUpperCase();

        if (placeRepository.existsByNumero(numeroNormalise)) {
            throw new IllegalArgumentException(
                "Ce numéro de place existe déjà."
            );
        }

        TypePlace type = typePlaceRepository
                .findById(typePlaceId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Type de place introuvable."
                    )
                );

        Place place = new Place();

        place.setNumero(numeroNormalise);
        place.setZone(zone);
        place.setType(type);
        place.setStatut(StatutPlace.LIBRE);

        return placeRepository.save(place);
    }

    // Lister toutes les places
    @Transactional(readOnly = true)
    public List<Place> lister() {
        return placeRepository.findAll();
    }

    // Rechercher par ID
    @Transactional(readOnly = true)
    public Place rechercherParId(Long id) {

        return placeRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Place introuvable : " + id
                    )
                );
    }

    // Lister les places selon leur statut
    @Transactional(readOnly = true)
    public List<Place> listerParStatut(StatutPlace statut) {

        return placeRepository.findByStatut(statut);
    }

    // Vérifier les réservations sur une période
    @Transactional(readOnly = true)
    public boolean estDisponible(
            Long placeId,
            LocalDateTime debut,
            LocalDateTime fin) {

        if (debut == null || fin == null
                || !debut.isBefore(fin)) {
            throw new IllegalArgumentException(
                "La période demandée est invalide."
            );
        }

        Place place = rechercherParId(placeId);

        if (place.getStatut() == StatutPlace.MAINTENANCE
                || place.getStatut() == StatutPlace.HORS_SERVICE) {
            return false;
        }

        boolean conflitReservation = reservationRepository
                .existsByPlace_IdAndStatutInAndDateDebutLessThanAndDateFinGreaterThan(
                    placeId,
                    List.of(
                        StatutReservation.EN_ATTENTE,
                        StatutReservation.CONFIRMEE
                    ),
                    fin,
                    debut
                );

        if (conflitReservation) {
            return false;
        }

        // Un stationnement en cours bloque la place
        // si la période demandée comprend l'instant présent.
        LocalDateTime maintenant = LocalDateTime.now();

        if (!debut.isAfter(maintenant)
                && fin.isAfter(maintenant)) {

            return !stationnementRepository
                    .existsByPlace_IdAndStatut(
                        placeId,
                        StatutStationnement.EN_COURS
                    );
        }

        return true;
    }

    @Transactional
    public void mettreEnMaintenance(Long id) {

        Place place = placeRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Place introuvable : " + id)
            );

        if (place.getStatut() != StatutPlace.LIBRE) {
            throw new IllegalStateException(
                "Seule une place libre peut être mise en maintenance."
            );
        }

        place.setStatut(StatutPlace.MAINTENANCE);

        placeRepository.save(place);
    }

    @Transactional
    public void remettreEnService(Long id) {

        Place place = placeRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Place introuvable : " + id)
            );

        if (place.getStatut() != StatutPlace.MAINTENANCE) {
            throw new IllegalStateException(
                "Cette place n'est pas en maintenance."
            );
        }

        place.setStatut(StatutPlace.LIBRE);

        placeRepository.save(place);
    }

}