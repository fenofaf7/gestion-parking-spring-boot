package com.exam.parking.repository;

import com.exam.parking.entity.Reservation;
import com.exam.parking.enumeration.StatutReservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPlace_Id(Long placeId);

    List<Reservation> findByStatut(StatutReservation statut);

    boolean existsByPlace_IdAndStatutInAndDateDebutLessThanAndDateFinGreaterThan(
            Long placeId,
            List<StatutReservation> statuts,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    long countByUtilisateur_Id(Long utilisateurId);

    @EntityGraph(attributePaths = {"vehicule", "place"})
    List<Reservation> findByUtilisateur_Id(Long utilisateurId);
}