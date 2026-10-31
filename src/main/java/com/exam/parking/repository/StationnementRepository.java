package com.exam.parking.repository;

import com.exam.parking.entity.Stationnement;
import com.exam.parking.enumeration.StatutStationnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationnementRepository extends JpaRepository<Stationnement, Long> {

    List<Stationnement> findByStatut(StatutStationnement statut);

    List<Stationnement> findByPlace_Id(Long placeId);

    List<Stationnement> findByVehicule_Id(Long vehiculeId);

    Optional<Stationnement> findByReservation_Id(Long reservationId);

    Optional<Stationnement> findByVehicule_IdAndStatut(
            Long vehiculeId,
            StatutStationnement statut
    );

    boolean existsByPlace_IdAndStatut(
            Long placeId,
            StatutStationnement statut
    );

    boolean existsByVehicule_IdAndStatut(
            Long vehiculeId,
            StatutStationnement statut
    );

}