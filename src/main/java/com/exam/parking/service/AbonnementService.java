package com.exam.parking.service;

import com.exam.parking.entity.Abonnement;
import com.exam.parking.entity.Place;
import com.exam.parking.entity.TypeAbonnement;
import com.exam.parking.entity.Utilisateur;

import com.exam.parking.enumeration.StatutAbonnement;
import com.exam.parking.enumeration.StatutPlace;
import com.exam.parking.enumeration.StatutStationnement;
import com.exam.parking.repository.AbonnementRepository;
import com.exam.parking.repository.PlaceRepository;
import com.exam.parking.repository.StationnementRepository;
import com.exam.parking.repository.TypeAbonnementRepository;
import com.exam.parking.repository.UtilisateurRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PlaceRepository placeRepository;
    private final StationnementRepository stationnementRepository;

    public AbonnementService(
            AbonnementRepository abonnementRepository,
            TypeAbonnementRepository typeAbonnementRepository,
            UtilisateurRepository utilisateurRepository,
            PlaceRepository placeRepository,
            StationnementRepository stationnementRepository) {

        this.abonnementRepository = abonnementRepository;
        this.typeAbonnementRepository = typeAbonnementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.placeRepository = placeRepository;
        this.stationnementRepository = stationnementRepository;
    }

    // Calculer la date de fin
    private LocalDate calculerDateFin(
            LocalDate debut,
            TypeAbonnement type) {

        ChronoUnit unite = type.getUniteDuree();

        if (unite != ChronoUnit.WEEKS
                && unite != ChronoUnit.MONTHS
                && unite != ChronoUnit.YEARS) {
            throw new IllegalArgumentException(
                "Unité de durée non prise en charge."
            );
        }

        return debut.plus(type.getDuree(), unite);
    }


	public Abonnement souscrire(
	        Long utilisateurId,
	        Long typeAbonnementId,
	        Long placeId) {

	    if (utilisateurId == null
	            || typeAbonnementId == null
	            || placeId == null) {
	        throw new IllegalArgumentException(
	            "Le client, la formule et la place sont obligatoires."
	        );
	    }

	    Utilisateur utilisateur = utilisateurRepository
	        .findById(utilisateurId)
	        .orElseThrow(() ->
	            new NoSuchElementException("Utilisateur introuvable.")
	        );

	    if (!utilisateur.isActif()) {
	        throw new IllegalStateException(
	            "Cet utilisateur est désactivé."
	        );
	    }

	    TypeAbonnement type = typeAbonnementRepository
	        .findById(typeAbonnementId)
	        .orElseThrow(() ->
	            new NoSuchElementException(
	                "Type d'abonnement introuvable."
	            )
	        );

	    if (!type.isActif()) {
	        throw new IllegalStateException(
	            "Cette formule n'est plus disponible."
	        );
	    }

	    if (type.getDuree() <= 0
	            || type.getPrix() == null
	            || type.getPrix().signum() < 0) {
	        throw new IllegalStateException(
	            "La durée ou le prix de la formule est invalide."
	        );
	    }

	    // Verrouiller la place pendant l'attribution.
	    Place place = placeRepository
	        .findByIdForUpdate(placeId)
	        .orElseThrow(() ->
	            new NoSuchElementException("Place introuvable.")
	        );

	    if (place.getStatut() != StatutPlace.LIBRE) {
	        throw new IllegalStateException(
	            "Cette place n'est pas physiquement libre."
	        );
	    }

	    if (stationnementRepository.existsByPlace_IdAndStatut(
	            placeId,
	            StatutStationnement.EN_COURS)) {
	        throw new IllegalStateException(
	            "Un stationnement est déjà en cours sur cette place."
	        );
	    }

	    LocalDate debut = LocalDate.now();
	    LocalDate fin = calculerDateFin(debut, type);

	    boolean dejaAbonne = abonnementRepository
	        .existsByUtilisateur_IdAndStatutAndDateDebutLessThanAndDateFinGreaterThan(
	            utilisateurId,
	            StatutAbonnement.ACTIF,
	            fin,
	            debut
	        );

	    if (dejaAbonne) {
	        throw new IllegalStateException(
	            "Ce client possède déjà un abonnement actif "
	            + "sur cette période."
	        );
	    }

	    boolean placeAttribuee = abonnementRepository
	        .existsByPlace_IdAndStatutAndDateDebutLessThanAndDateFinGreaterThan(
	            placeId,
	            StatutAbonnement.ACTIF,
	            fin,
	            debut
	        );

	    if (placeAttribuee) {
	        throw new IllegalStateException(
	            "Cette place est déjà attribuée à un autre abonnement."
	        );
	    }

	    Abonnement abonnement = new Abonnement();

	    abonnement.setUtilisateur(utilisateur);
	    abonnement.setType(type);
	    abonnement.setPlace(place);
	    abonnement.setDateDebut(debut);
	    abonnement.setDateFin(fin);
	    abonnement.setPrix(type.getPrix());
	    abonnement.setStatut(StatutAbonnement.ACTIF);

	    return abonnementRepository.save(abonnement);
	}


    @Transactional(readOnly = true)
    public List<Abonnement> lister() {
        return abonnementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Abonnement rechercherParId(Long id) {
        return abonnementRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Abonnement introuvable : " + id
                    )
                );
    }

    @Transactional(readOnly = true)
    public List<Abonnement> listerParUtilisateur(Long utilisateurId) {
        return abonnementRepository
                .findByUtilisateur_Id(utilisateurId);
    }

    @Transactional(readOnly = true)
    public boolean estValide(Long abonnementId) {
        Abonnement abonnement = rechercherParId(abonnementId);
        return abonnement.estValide(LocalDate.now());
    }

    // Renouveler un abonnement
    public Abonnement renouveler(Long abonnementId) {

        Abonnement abonnement = rechercherParId(abonnementId);

        if (abonnement.getStatut() == StatutAbonnement.ANNULE) {
            throw new IllegalStateException(
                "Un abonnement annulé ne peut pas être renouvelé."
            );
        }

        TypeAbonnement type = abonnement.getType();

        if (!type.isActif()) {
            throw new IllegalStateException(
                "La formule n'est plus disponible."
            );
        }

        LocalDate aujourdHui = LocalDate.now();

        LocalDate nouveauDebut =
                abonnement.getDateFin().isAfter(aujourdHui)
                ? abonnement.getDateFin()
                : aujourdHui;

        LocalDate nouvelleFin =
                calculerDateFin(nouveauDebut, type);

        abonnement.setDateFin(nouvelleFin);
        abonnement.setStatut(StatutAbonnement.ACTIF);

        return abonnementRepository.save(abonnement);
    }

    // Annuler un abonnement
    public Abonnement annuler(Long abonnementId) {

        Abonnement abonnement = rechercherParId(abonnementId);

        if (abonnement.getStatut() == StatutAbonnement.ANNULE) {
            throw new IllegalStateException(
                "Cet abonnement est déjà annulé."
            );
        }

        abonnement.setStatut(StatutAbonnement.ANNULE);

        return abonnementRepository.save(abonnement);
    }
}