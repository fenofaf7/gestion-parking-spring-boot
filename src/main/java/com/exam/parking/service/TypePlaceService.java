package com.exam.parking.service;

import com.exam.parking.entity.TypePlace;
import com.exam.parking.repository.TypePlaceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class TypePlaceService {

    private final TypePlaceRepository typePlaceRepository;

    public TypePlaceService(TypePlaceRepository typePlaceRepository) {
        this.typePlaceRepository = typePlaceRepository;
    }

    public TypePlace ajouter(
            String nom,
            String description,
            BigDecimal tarifHoraireBase,
            boolean reserveeVIP) {

        String nomNormalise = normaliserNom(nom);
        verifierPrix(tarifHoraireBase);

        if (typePlaceRepository.findByNom(nomNormalise).isPresent()) {
            throw new IllegalArgumentException(
                "Ce type de place existe déjà."
            );
        }

        TypePlace type = new TypePlace(
            nomNormalise,
            description,
            tarifHoraireBase,
            reserveeVIP
        );

        return typePlaceRepository.save(type);
    }

    @Transactional(readOnly = true)
    public List<TypePlace> lister() {
        return typePlaceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TypePlace rechercherParId(Long id) {
        return typePlaceRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Type de place introuvable : " + id
                )
            );
    }

    @Transactional(readOnly = true)
    public List<TypePlace> listerNonVIP() {
        return typePlaceRepository.findByReserveeVIPFalse();
    }

    public TypePlace modifier(
            Long id,
            String nom,
            String description,
            BigDecimal tarifHoraireBase,
            boolean reserveeVIP) {

        TypePlace type = rechercherParId(id);

        String nomNormalise = normaliserNom(nom);
        verifierPrix(tarifHoraireBase);

        typePlaceRepository.findByNom(nomNormalise)
            .ifPresent(existant -> {
                if (!existant.getId().equals(id)) {
                    throw new IllegalArgumentException(
                        "Ce nom est déjà utilisé."
                    );
                }
            });

        type.setNom(nomNormalise);
        type.setDescription(description);
        type.setTarifHoraireBase(tarifHoraireBase);
        type.setReserveeVIP(reserveeVIP);

        return typePlaceRepository.save(type);
    }

    private String normaliserNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du type de place est obligatoire."
            );
        }

        return nom.trim().toUpperCase();
    }

    private void verifierPrix(BigDecimal prix) {

        if (prix == null
                || prix.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                "Le tarif horaire ne peut pas être négatif."
            );
        }
    }
}