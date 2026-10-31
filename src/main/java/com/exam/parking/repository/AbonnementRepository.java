
package com.exam.parking.repository;

import com.exam.parking.entity.Abonnement;
import com.exam.parking.enumeration.StatutAbonnement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AbonnementRepository
        extends JpaRepository<Abonnement, Long> {


    List<Abonnement> findByUtilisateur_Id(Long utilisateurId);

    List<Abonnement> findByStatut(StatutAbonnement statut);


    boolean existsByUtilisateur_IdAndStatutAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            Long utilisateurId,
            StatutAbonnement statut,
            LocalDate dateDebut,
            LocalDate dateFin
    );


    boolean existsByUtilisateur_IdAndStatutAndDateDebutLessThanAndDateFinGreaterThan(
            Long utilisateurId,
            StatutAbonnement statut,
            LocalDate nouvelleFin,
            LocalDate nouveauDebut
    );


    boolean existsByPlace_IdAndStatutAndDateDebutLessThanAndDateFinGreaterThan(
            Long placeId,
            StatutAbonnement statut,
            LocalDate nouvelleFin,
            LocalDate nouveauDebut
    );

    long countByUtilisateur_Id(Long utilisateurId);
}
