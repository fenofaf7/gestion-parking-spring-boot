package com.exam.parking.service;

import com.exam.parking.entity.Paiement;
import com.exam.parking.entity.Stationnement;

import com.exam.parking.enumeration.ModePaiement;
import com.exam.parking.enumeration.StatutPaiement;
import com.exam.parking.enumeration.StatutStationnement;

import com.exam.parking.repository.PaiementRepository;
import com.exam.parking.repository.StationnementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final StationnementRepository stationnementRepository;

    public PaiementService(
            PaiementRepository paiementRepository,
            StationnementRepository stationnementRepository) {

        this.paiementRepository = paiementRepository;
        this.stationnementRepository = stationnementRepository;
    }

    // Créer un paiement pour un stationnement
    public Paiement creerPaiement(
            Long stationnementId,
            ModePaiement modePaiement) {

        if (modePaiement == null) {
            throw new IllegalArgumentException(
                "Le mode de paiement est obligatoire."
            );
        }

        Stationnement stationnement = stationnementRepository
                .findById(stationnementId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Stationnement introuvable."
                    )
                );

        if (stationnement.getStatut()
                != StatutStationnement.TERMINE) {
            throw new IllegalStateException(
                "Le stationnement doit être terminé."
            );
        }

        BigDecimal montant = stationnement.getMontantTotal();

        if (montant == null || montant.signum() < 0) {
            throw new IllegalStateException(
                "Le montant du stationnement est invalide."
            );
        }

        boolean dejaPaye = paiementRepository
                .findByStationnement_Id(stationnementId)
                .stream()
                .anyMatch(p ->
                    p.getStatut() == StatutPaiement.CONFIRME
                );

        if (dejaPaye) {
            throw new IllegalStateException(
                "Ce stationnement est déjà payé."
            );
        }

        boolean paiementEnAttente = paiementRepository
        	    .findByStationnement_Id(stationnementId)
        	    .stream()
        	    .anyMatch(p ->
        	        p.getStatut() == StatutPaiement.EN_ATTENTE
        	    );

        	if (paiementEnAttente) {
        	    throw new IllegalStateException(
        	        "Un paiement est déjà en attente pour ce stationnement."
        	    );
        }

        Paiement paiement = new Paiement();

        paiement.setReference(
                "PAY-" + UUID.randomUUID()
        );
        paiement.setStationnement(stationnement);
        paiement.setMontant(montant);
        paiement.setMode(modePaiement);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);

        return paiementRepository.save(paiement);
    }


    public Paiement confirmerPaiement(Long paiementId) {

        Paiement paiement = paiementRepository
                .findById(paiementId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Paiement introuvable."
                    )
                );

        boolean autrePaiementConfirme = paiementRepository
        	    .findByStationnement_Id(paiement.getStationnement().getId())
        	    .stream()
        	    .anyMatch(p ->
        	        !p.getId().equals(paiementId)
        	        && p.getStatut() == StatutPaiement.CONFIRME
        	    );

        	if (autrePaiementConfirme) {
        	    throw new IllegalStateException(
        	        "Ce stationnement possède déjà un paiement confirmé."
        	    );
        }

        if (paiement.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new IllegalStateException(
                "Ce paiement ne peut pas être confirmé."
            );
        }

        paiement.setStatut(StatutPaiement.CONFIRME);
        paiement.setDatePaiement(LocalDateTime.now());

        return paiementRepository.save(paiement);
    }

    @Transactional(readOnly = true)
    public Paiement rechercherParId(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Paiement introuvable : " + id
                    )
                );
    }

    @Transactional(readOnly = true)
    public List<Paiement> lister() {
        return paiementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Paiement> listerParStationnement(
            Long stationnementId) {

        return paiementRepository
                .findByStationnement_Id(stationnementId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerRecettesEncaissees() {
        return paiementRepository.calculerTotalParStatut(
                StatutPaiement.CONFIRME
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerMontantEnAttente() {
        return paiementRepository.calculerTotalParStatut(
                StatutPaiement.EN_ATTENTE
        );
    }

    @Transactional(readOnly = true)
    public long compterPaiementsConfirmes() {
        return paiementRepository.countByStatut(
                StatutPaiement.CONFIRME
        );
    }

    @Transactional(readOnly = true)
    public long compterPaiementsEnAttente() {
        return paiementRepository.countByStatut(
                StatutPaiement.EN_ATTENTE
        );
    }

    @Transactional(readOnly = true)
    public long compterPaiementsEchoues() {
        return paiementRepository.countByStatut(
                StatutPaiement.ECHEC
        );
    }
}