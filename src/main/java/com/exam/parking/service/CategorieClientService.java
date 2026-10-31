package com.exam.parking.service;

import com.exam.parking.entity.CategorieClient;
import com.exam.parking.enumeration.CategorieClientType;
import com.exam.parking.repository.CategorieClientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CategorieClientService {

    private final CategorieClientRepository categorieRepository;

    public CategorieClientService(
            CategorieClientRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public CategorieClient ajouter(
            CategorieClientType nom,
            String description,
            BigDecimal coefficientRemise,
            boolean avantage) {

        verifierRemise(coefficientRemise);

        if (nom == null) {
            throw new IllegalArgumentException(
                "La catégorie est obligatoire."
            );
        }

        if (categorieRepository.existsByNom(nom)) {
            throw new IllegalArgumentException(
                "Cette catégorie existe déjà."
            );
        }

        CategorieClient categorie = new CategorieClient(
            nom,
            description,
            coefficientRemise,
            avantage
        );

        return categorieRepository.save(categorie);
    }

    @Transactional(readOnly = true)
    public List<CategorieClient> lister() {
        return categorieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CategorieClient rechercherParId(Long id) {
        return categorieRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Catégorie introuvable : " + id
                )
            );
    }

    @Transactional(readOnly = true)
    public CategorieClient rechercherParNom(
            CategorieClientType nom) {

        return categorieRepository.findByNom(nom)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Catégorie introuvable : " + nom
                )
            );
    }

    public CategorieClient modifier(
            Long id,
            String description,
            BigDecimal coefficientRemise,
            boolean avantage) {

        verifierRemise(coefficientRemise);

        CategorieClient categorie = rechercherParId(id);

        categorie.setDescription(description);
        categorie.setCoefficientRemise(coefficientRemise);
        categorie.setAvantage(avantage);

        return categorieRepository.save(categorie);
    }

    private void verifierRemise(BigDecimal remise) {

        if (remise == null
                || remise.compareTo(BigDecimal.ZERO) < 0
                || remise.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                "La remise doit être comprise entre 0 et 1."
            );
        }
    }
}