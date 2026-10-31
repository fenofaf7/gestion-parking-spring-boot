package com.exam.parking.repository;

import com.exam.parking.entity.Place;
import com.exam.parking.enumeration.StatutPlace;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository
        extends JpaRepository<Place, Long> {

    Optional<Place> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Place> findByStatut(StatutPlace statut);

    List<Place> findByZone(String zone);

    List<Place> findByType_Id(Long typePlaceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Place p WHERE p.id = :id")
    Optional<Place> findByIdForUpdate(@Param("id") Long id);
}