package com.exam.parking.repository;

import com.exam.parking.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    List<Vehicule> findByProprietaire_Id(Long utilisateurId);

    long countByProprietaire_Id(Long utilisateurId);
}