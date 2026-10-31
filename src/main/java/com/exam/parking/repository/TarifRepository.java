package com.exam.parking.repository;

import com.exam.parking.entity.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifRepository extends JpaRepository<Tarif, Long> {

    List<Tarif> findByTypePlace_Id(Long typePlaceId);

    List<Tarif> findByActifTrue();

    List<Tarif> findByTypePlace_IdAndActifTrue(Long typePlaceId);
}