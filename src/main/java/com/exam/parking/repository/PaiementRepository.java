package com.exam.parking.repository;

import com.exam.parking.entity.Paiement;
import com.exam.parking.enumeration.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    Optional<Paiement> findByReference(String reference);

    List<Paiement> findByStationnement_Id(Long stationnementId);

    List<Paiement> findByStatut(StatutPaiement statut);

    @Query("""
    	    SELECT COALESCE(SUM(p.montant), 0)
    	    FROM Paiement p
    	    WHERE p.statut = :statut
    	""")
    	BigDecimal calculerTotalParStatut(
    	        @Param("statut") StatutPaiement statut
    	);

    long countByStatut(StatutPaiement statut);

    long countByStationnement_Vehicule_Proprietaire_IdAndStatut(
            Long utilisateurId,
            StatutPaiement statut
    );
}