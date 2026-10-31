package com.exam.parking.service;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.entity.Vehicule;
import com.exam.parking.repository.UtilisateurRepository;
import com.exam.parking.repository.VehiculeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@Transactional
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public VehiculeService(
            VehiculeRepository vehiculeRepository,
            UtilisateurRepository utilisateurRepository) {

        this.vehiculeRepository = vehiculeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public Vehicule ajouter(
            String immatriculation,
            String marque,
            String modele,
            String couleur,
            Long proprietaireId) {

        String plaque = normaliserImmatriculation(immatriculation);

        if (vehiculeRepository.existsByImmatriculation(plaque)) {
            throw new IllegalArgumentException(
                "Cette immatriculation existe déjà."
            );
        }

        Utilisateur proprietaire = null;

        if (proprietaireId != null) {
            proprietaire = utilisateurRepository
                .findById(proprietaireId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Propriétaire introuvable."
                    )
                );
        }

        Vehicule vehicule = new Vehicule(
            plaque,
            marque,
            modele,
            couleur,
            proprietaire
        );

        return vehiculeRepository.save(vehicule);
    }

    @Transactional(readOnly = true)
    public List<Vehicule> lister() {
        return vehiculeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicule rechercherParId(Long id) {
        return vehiculeRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Véhicule introuvable : " + id
                )
            );
    }

    @Transactional(readOnly = true)
    public Vehicule rechercherParImmatriculation(
            String immatriculation) {

        String plaque = normaliserImmatriculation(immatriculation);

        return vehiculeRepository
            .findByImmatriculation(plaque)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Aucun véhicule avec cette immatriculation."
                )
            );
    }

    @Transactional(readOnly = true)
    public List<Vehicule> listerParProprietaire(
            Long proprietaireId) {

        return vehiculeRepository
            .findByProprietaire_Id(proprietaireId);
    }

    public Vehicule modifier(
            Long id,
            String marque,
            String modele,
            String couleur) {

        Vehicule vehicule = rechercherParId(id);

        vehicule.setMarque(marque);
        vehicule.setModele(modele);
        vehicule.setCouleur(couleur);

        return vehiculeRepository.save(vehicule);
    }

    public Vehicule changerProprietaire(
            Long vehiculeId,
            Long proprietaireId) {

        Vehicule vehicule = rechercherParId(vehiculeId);

        Utilisateur proprietaire = null;

        if (proprietaireId != null) {
            proprietaire = utilisateurRepository
                .findById(proprietaireId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Propriétaire introuvable."
                    )
                );
        }

        vehicule.setProprietaire(proprietaire);

        return vehiculeRepository.save(vehicule);
    }


	public Vehicule modifierPourClient(
	        Long vehiculeId,
	        Long clientId,
	        String marque,
	        String modele,
	        String couleur) {

	    Vehicule vehicule = vehiculeRepository
	            .findById(vehiculeId)
	            .orElseThrow(() ->
	                    new NoSuchElementException(
	                            "Véhicule introuvable."
	                    )
	            );

	    if (vehicule.getProprietaire() == null
	            || !vehicule.getProprietaire()
	                    .getId().equals(clientId)) {

	        throw new org.springframework.security.access
	                .AccessDeniedException(
	                        "Ce véhicule ne vous appartient pas."
	                );
	    }

	    vehicule.setMarque(marque);
	    vehicule.setModele(modele);
	    vehicule.setCouleur(couleur);

	    return vehiculeRepository.save(vehicule);
	}


    private String normaliserImmatriculation(String immatriculation) {

        if (immatriculation == null
                || immatriculation.isBlank()) {
            throw new IllegalArgumentException(
                "L'immatriculation est obligatoire."
            );
        }

        return immatriculation.trim().toUpperCase(Locale.ROOT);
    }
}