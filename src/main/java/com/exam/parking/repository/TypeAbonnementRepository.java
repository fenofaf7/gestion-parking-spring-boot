package com.exam.parking.repository;

import com.exam.parking.entity.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TypeAbonnementRepository extends JpaRepository<TypeAbonnement, Long> {

    Optional<TypeAbonnement> findByNom(String nom);

    List<TypeAbonnement> findByActifTrue();
}