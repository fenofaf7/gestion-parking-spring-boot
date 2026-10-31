package com.exam.parking.repository;

import com.exam.parking.entity.TypePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TypePlaceRepository extends JpaRepository<TypePlace, Long> {

    Optional<TypePlace> findByNom(String nom);

    List<TypePlace> findByReserveeVIPFalse();
}