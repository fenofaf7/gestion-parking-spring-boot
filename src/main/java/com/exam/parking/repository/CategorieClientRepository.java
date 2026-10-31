package com.exam.parking.repository;

import com.exam.parking.entity.CategorieClient;
import com.exam.parking.enumeration.CategorieClientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieClientRepository
        extends JpaRepository<CategorieClient, Long> {

    Optional<CategorieClient> findByNom(CategorieClientType nom);

    boolean existsByNom(CategorieClientType nom);
}