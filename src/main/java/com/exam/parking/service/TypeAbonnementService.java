package com.exam.parking.service;

import com.exam.parking.entity.TypeAbonnement;
import com.exam.parking.repository.TypeAbonnementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class TypeAbonnementService {

    private final TypeAbonnementRepository typeAbonnementRepository;

    public TypeAbonnementService(
            TypeAbonnementRepository typeAbonnementRepository) {

        this.typeAbonnementRepository = typeAbonnementRepository;
    }

    public TypeAbonnement ajouter(
            String nom,
            String description,
            int duree,
            ChronoUnit uniteDuree,
            BigDecimal prix) {

        String nomNormalise = normaliserNom(nom);
        verifierFormule(duree, uniteDuree, prix);

        if (typeAbonnementRepository.findByNom(nomNormalise)
                .isPresent()) {

            throw new IllegalArgumentException(
                "Cette formule existe déjà."
            );
        }

        TypeAbonnement type = new TypeAbonnement(
            nomNormalise,
            description,
            duree,
            uniteDuree,
            prix
        );

        return typeAbonnementRepository.save(type);
    }

    @Transactional(readOnly = true)
    public List<TypeAbonnement> lister() {
        return typeAbonnementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TypeAbonnement> listerActifs() {
        return typeAbonnementRepository.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public TypeAbonnement rechercherParId(Long id) {
        return typeAbonnementRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Formule d'abonnement introuvable : " + id
                )
            );
    }

    public TypeAbonnement modifier(
            Long id,
            String description,
            int duree,
            ChronoUnit uniteDuree,
            BigDecimal prix) {

        verifierFormule(duree, uniteDuree, prix);

        TypeAbonnement type = rechercherParId(id);

        type.setDescription(description);
        type.setDuree(duree);
        type.setUniteDuree(uniteDuree);
        type.setPrix(prix);

        return typeAbonnementRepository.save(type);
    }

    public TypeAbonnement desactiver(Long id) {

        TypeAbonnement type = rechercherParId(id);

        type.setActif(false);

        return typeAbonnementRepository.save(type);
    }

    public TypeAbonnement reactiver(Long id) {

        TypeAbonnement type = rechercherParId(id);

        type.setActif(true);

        return typeAbonnementRepository.save(type);
    }

    private String normaliserNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                "Le nom de la formule est obligatoire."
            );
        }

        return nom.trim().toUpperCase();
    }

    private void verifierFormule(
            int duree,
            ChronoUnit uniteDuree,
            BigDecimal prix) {

        if (duree <= 0) {
            throw new IllegalArgumentException(
                "La durée doit être supérieure à zéro."
            );
        }

        if (uniteDuree != ChronoUnit.WEEKS
                && uniteDuree != ChronoUnit.MONTHS
                && uniteDuree != ChronoUnit.YEARS) {

            throw new IllegalArgumentException(
                "Unité autorisée : WEEKS, MONTHS ou YEARS."
            );
        }

        if (prix == null
                || prix.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                "Le prix ne peut pas être négatif."
            );
        }
    }
}